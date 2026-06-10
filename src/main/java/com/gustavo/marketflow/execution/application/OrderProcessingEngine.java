package com.gustavo.marketflow.execution.application;

import com.gustavo.marketflow.execution.domain.OrderEnqueueStatus;
import com.gustavo.marketflow.execution.domain.OrderQueue;
import com.gustavo.marketflow.execution.domain.QueuedOrder;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.shared.exception.OrderAlreadyQueuedException;
import com.gustavo.marketflow.shared.exception.OrderQueueFullException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coordinates queueing, worker lifecycle and asynchronous order processing.
 *
 * <p>The engine is deliberately process-local in this phase. It exposes the
 * minimal operations needed to demonstrate producer-consumer processing with
 * a managed {@link ExecutorService} and a bounded {@link OrderQueue}.</p>
 */
@Service
public class OrderProcessingEngine {

    private static final Logger log = LoggerFactory.getLogger(OrderProcessingEngine.class);

    private final OrderExecutionService orderExecutionService;
    private final OrderQueue orderQueue;
    private final ExecutionProperties executionProperties;
    private final ExecutionStatistics executionStatistics;
    private final ExecutorService executionWorkerExecutor;
    private final List<Future<?>> workerFutures;
    private final AtomicBoolean running;
    private final Counter queuedCounter;
    private final Counter succeededCounter;
    private final Counter failedCounter;
    private final Timer processingTimer;

    public OrderProcessingEngine(OrderExecutionService orderExecutionService,
                                 OrderQueue orderQueue,
                                 ExecutionProperties executionProperties,
                                 @Qualifier("executionWorkerExecutor") ExecutorService executionWorkerExecutor,
                                 MeterRegistry meterRegistry) {
        this.orderExecutionService = orderExecutionService;
        this.orderQueue = orderQueue;
        this.executionProperties = executionProperties;
        this.executionStatistics = new ExecutionStatistics();
        this.executionWorkerExecutor = executionWorkerExecutor;
        this.workerFutures = new CopyOnWriteArrayList<>();
        this.running = new AtomicBoolean(false);
        this.queuedCounter = meterRegistry.counter("marketflow.order.queued");
        this.succeededCounter = meterRegistry.counter("marketflow.order.processed.success");
        this.failedCounter = meterRegistry.counter("marketflow.order.processed.failure");
        this.processingTimer = meterRegistry.timer("marketflow.order.processing.duration");
        meterRegistry.gauge("marketflow.order.queue.size", orderQueue, OrderQueue::size);
    }

    public void enqueue(UUID orderId) {
        Order order = orderExecutionService.requireQueueable(orderId);
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        MDC.put("orderId", order.getId().toString());
        MDC.put("clientId", order.getClientId());
        try {
            OrderEnqueueStatus enqueueStatus = orderQueue.enqueue(QueuedOrder.capture(orderId));
            if (enqueueStatus == OrderEnqueueStatus.DUPLICATE) {
                throw new OrderAlreadyQueuedException(orderId);
            }
            if (enqueueStatus == OrderEnqueueStatus.FULL) {
                throw new OrderQueueFullException(orderId);
            }

            orderExecutionService.recordQueued(orderId);
            executionStatistics.recordQueued();
            queuedCounter.increment();
            log.info("Order {} queued for asynchronous processing", orderId);
        } finally {
            MDC.remove("orderId");
            MDC.remove("clientId");
            restoreMdc(previousContext);
        }
    }

    public void start() {
        if (!running.compareAndSet(false, true)) {
            log.info("Execution engine start ignored because workers are already running");
            return;
        }

        for (int i = 0; i < executionProperties.workerCount(); i++) {
            workerFutures.add(executionWorkerExecutor.submit(new ExecutionWorker(running, orderQueue, this)));
        }
        log.info("Execution engine started with {} workers", executionProperties.workerCount());
    }

    public void stop() {
        if (!running.compareAndSet(true, false)) {
            log.info("Execution engine stop ignored because workers are already stopped");
            return;
        }

        for (Future<?> workerFuture : workerFutures) {
            workerFuture.cancel(true);
        }
        workerFutures.clear();
        log.info("Execution engine stopped");
    }

    public ExecutionStats getStats() {
        int activeWorkers = (int) workerFutures.stream()
                .filter(workerFuture -> !workerFuture.isDone())
                .count();
        return executionStatistics.snapshot(
                running.get(),
                executionProperties.workerCount(),
                activeWorkers,
                orderQueue.size()
        );
    }

    void handleQueuedOrder(QueuedOrder queuedOrder) {
        Instant startedAt = Instant.now();
        try {
            Order acceptedOrder = orderExecutionService.markAccepted(queuedOrder.orderId());
            boolean successful = shouldSucceed(acceptedOrder);
            pauseIfConfigured();

            ExecutionResult executionResult;
            if (successful) {
                Order executedOrder = orderExecutionService.markExecuted(queuedOrder.orderId());
                executionResult = new ExecutionResult(
                        executedOrder.getId(),
                        OrderStatus.ACCEPTED,
                        executedOrder.getStatus(),
                        true,
                        Thread.currentThread().getName(),
                        "EXECUTED",
                        Instant.now()
                );
                executionStatistics.recordProcessed(true);
                succeededCounter.increment();
            } else {
                Order failedOrder = orderExecutionService.markFailed(queuedOrder.orderId(), "Simulated processing failure");
                executionResult = new ExecutionResult(
                        failedOrder.getId(),
                        OrderStatus.ACCEPTED,
                        failedOrder.getStatus(),
                        false,
                        Thread.currentThread().getName(),
                        "FAILED",
                        Instant.now()
                );
                executionStatistics.recordProcessed(false);
                failedCounter.increment();
            }
            log.info("Order {} processed result={} worker={}",
                    executionResult.orderId(), executionResult.outcome(), executionResult.workerName());
        } catch (RuntimeException ex) {
            executionStatistics.recordProcessed(false);
            failedCounter.increment();
            log.error("Order {} processing failed unexpectedly", queuedOrder.orderId(), ex);
            try {
                orderExecutionService.markFailed(queuedOrder.orderId(), "Unhandled worker exception");
            } catch (RuntimeException markFailedException) {
                log.warn("Order {} could not be marked as failed after worker exception", queuedOrder.orderId(), markFailedException);
            }
        } finally {
            processingTimer.record(java.time.Duration.between(startedAt, Instant.now()));
            orderQueue.markProcessed(queuedOrder.orderId());
        }
    }

    @PreDestroy
    public void shutdown() {
        stop();
        executionWorkerExecutor.shutdownNow();
        try {
            executionWorkerExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean shouldSucceed(Order order) {
        return !"FAIL".equalsIgnoreCase(order.getSymbol());
    }

    private void pauseIfConfigured() {
        if (executionProperties.processingDelayMillis() <= 0) {
            return;
        }

        try {
            Thread.sleep(executionProperties.processingDelayMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void restoreMdc(Map<String, String> contextMap) {
        if (contextMap == null || contextMap.isEmpty()) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(contextMap);
    }
}
