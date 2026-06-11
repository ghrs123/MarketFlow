package com.gustavo.marketflow.execution.application;

import com.gustavo.marketflow.event.domain.OrderExecutedEvent;
import com.gustavo.marketflow.event.domain.OrderFailedEvent;
import com.gustavo.marketflow.event.domain.OrderMovedToDeadLetterEvent;
import com.gustavo.marketflow.event.domain.OrderQueuedEvent;
import com.gustavo.marketflow.event.domain.OrderRetriedEvent;
import com.gustavo.marketflow.event.infrastructure.InMemoryEventBus;
import com.gustavo.marketflow.execution.domain.DeadLetterMessage;
import com.gustavo.marketflow.execution.domain.OrderEnqueueStatus;
import com.gustavo.marketflow.execution.domain.OrderQueue;
import com.gustavo.marketflow.execution.domain.QueuedOrder;
import com.gustavo.marketflow.execution.domain.RetryPolicy;
import com.gustavo.marketflow.monitoring.application.AuditLogService;
import com.gustavo.marketflow.monitoring.application.OrderMetricsService;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.shared.exception.OrderAlreadyQueuedException;
import com.gustavo.marketflow.shared.exception.DeadLetterMessageNotFoundException;
import com.gustavo.marketflow.shared.exception.OrderQueueFullException;
import com.gustavo.marketflow.shared.logging.MdcContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
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
    private final RetryRegistry retryRegistry;
    private final DeadLetterQueue deadLetterQueue;
    private final InMemoryEventBus eventBus;
    private final RetryPolicy retryPolicy;
    private final ExecutorService executionWorkerExecutor;
    private final OrderMetricsService orderMetricsService;
    private final AuditLogService auditLogService;
    private final List<Future<?>> workerFutures;
    private final AtomicBoolean running;
    private final Timer processingTimer;
    private final Bulkhead orderProcessingBulkhead;

    public OrderProcessingEngine(OrderExecutionService orderExecutionService,
                                 OrderQueue orderQueue,
                                 ExecutionProperties executionProperties,
                                 RetryRegistry retryRegistry,
                                 DeadLetterQueue deadLetterQueue,
                                 InMemoryEventBus eventBus,
                                 @Qualifier("executionWorkerExecutor") ExecutorService executionWorkerExecutor,
                                 OrderMetricsService orderMetricsService,
                                 AuditLogService auditLogService,
                                 MeterRegistry meterRegistry,
                                 BulkheadRegistry bulkheadRegistry) {
        this.orderExecutionService = orderExecutionService;
        this.orderQueue = orderQueue;
        this.executionProperties = executionProperties;
        this.executionStatistics = new ExecutionStatistics();
        this.retryRegistry = retryRegistry;
        this.deadLetterQueue = deadLetterQueue;
        this.eventBus = eventBus;
        this.retryPolicy = new RetryPolicy(
                executionProperties.retryMaxAttempts(),
                executionProperties.retryInitialBackoffMillis(),
                executionProperties.retryMaxBackoffMillis()
        );
        this.executionWorkerExecutor = executionWorkerExecutor;
        this.orderMetricsService = orderMetricsService;
        this.auditLogService = auditLogService;
        this.workerFutures = new CopyOnWriteArrayList<>();
        this.running = new AtomicBoolean(false);
        this.processingTimer = meterRegistry.timer("marketflow.order.processing.duration");
        this.orderProcessingBulkhead = bulkheadRegistry.bulkhead("orderProcessing");
        meterRegistry.gauge("marketflow.order.queue.size", orderQueue, OrderQueue::size);
        meterRegistry.gauge("marketflow.dead.letter.queue.size", deadLetterQueue, DeadLetterQueue::size);
        meterRegistry.gauge("marketflow.active.workers", this, OrderProcessingEngine::activeWorkerCount);
    }

    public void enqueue(UUID orderId) {
        Order order = orderExecutionService.requireQueueable(orderId);
        Map<String, String> context = MDC.getCopyOfContextMap() == null
                ? new java.util.HashMap<>()
                : new java.util.HashMap<>(MDC.getCopyOfContextMap());
        context.put("orderId", order.getId().toString());
        context.put("clientId", order.getClientId());
        MdcContext.runWith(context, () -> {
            OrderEnqueueStatus enqueueStatus = orderQueue.enqueue(QueuedOrder.capture(orderId));
            if (enqueueStatus == OrderEnqueueStatus.DUPLICATE) {
                throw new OrderAlreadyQueuedException(orderId);
            }
            if (enqueueStatus == OrderEnqueueStatus.FULL) {
                throw new OrderQueueFullException(orderId);
            }

            orderExecutionService.recordQueued(orderId);
            eventBus.publish(OrderQueuedEvent.now(orderId));
            executionStatistics.recordQueued();
            orderMetricsService.recordQueued();
            auditLogService.recordOrderEvent("ORDER_QUEUED", orderId, "QUEUED");
            log.info("Order {} queued for asynchronous processing", orderId);
        });
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

    public List<DeadLetterMessage> getDeadLetters() {
        return deadLetterQueue.findAll();
    }

    public void reprocessDeadLetter(UUID orderId) {
        DeadLetterMessage deadLetterMessage = deadLetterQueue.remove(orderId)
                .orElseThrow(() -> new DeadLetterMessageNotFoundException(orderId));
        retryRegistry.clear(orderId);
        orderExecutionService.resetForReprocessing(orderId);
        try {
            enqueue(orderId);
            auditLogService.recordOrderEvent("DLQ_REPROCESS", orderId, "QUEUED");
            log.info("Dead-letter order {} queued for reprocessing", orderId);
        } catch (RuntimeException ex) {
            deadLetterQueue.add(deadLetterMessage);
            throw ex;
        }
    }

    void handleQueuedOrder(QueuedOrder queuedOrder) {
        Instant startedAt = Instant.now();
        try {
            Bulkhead.decorateRunnable(
                    orderProcessingBulkhead,
                    () -> processQueuedOrder(queuedOrder)
            ).run();
        } catch (BulkheadFullException ex) {
            executionStatistics.recordProcessed(false);
            orderMetricsService.recordFailed();
            log.warn("Order {} rejected by processing bulkhead", queuedOrder.orderId());
            orderExecutionService.markFailed(queuedOrder.orderId(), "Processing capacity exhausted");
        } catch (RuntimeException ex) {
            executionStatistics.recordProcessed(false);
            orderMetricsService.recordFailed();
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

    private void processQueuedOrder(QueuedOrder queuedOrder) {
        Order acceptedOrder = orderExecutionService.markAccepted(queuedOrder.orderId());
        ExecutionResult executionResult = processWithRetry(acceptedOrder, queuedOrder);
        log.info("Order {} processed result={} worker={}",
                executionResult.orderId(), executionResult.outcome(), executionResult.workerName());
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

    private ExecutionResult processWithRetry(Order order, QueuedOrder queuedOrder) {
        while (true) {
            int attempt = retryRegistry.recordAttempt(order.getId());
            pauseIfConfigured();
            if (shouldSucceed(order)) {
                Order executedOrder = orderExecutionService.markExecuted(order.getId());
                retryRegistry.clear(order.getId());
                eventBus.publish(OrderExecutedEvent.now(order.getId()));
                executionStatistics.recordProcessed(true);
                orderMetricsService.recordExecuted();
                auditLogService.recordOrderEvent("ORDER_EXECUTED", order.getId(), "EXECUTED");
                return result(executedOrder, true, "EXECUTED");
            }

            String reason = "Simulated processing failure";
            eventBus.publish(OrderFailedEvent.now(order.getId(), attempt, reason));
            if (!retryPolicy.canRetry(attempt)) {
                Order failedOrder = orderExecutionService.markFailed(order.getId(), reason);
                deadLetterQueue.add(DeadLetterMessage.create(
                        order.getId(),
                        reason,
                        attempt,
                        queuedOrder.mdcContext()
                ));
                eventBus.publish(OrderMovedToDeadLetterEvent.now(order.getId(), attempt));
                orderMetricsService.recordDeadLettered();
                executionStatistics.recordProcessed(false);
                orderMetricsService.recordFailed();
                auditLogService.recordOrderEvent("ORDER_DLQ", order.getId(), "DEAD_LETTERED");
                return result(failedOrder, false, "DEAD_LETTERED");
            }

            long backoffMillis = retryPolicy.backoffMillisFor(attempt);
            orderExecutionService.recordRetry(order.getId(), attempt + 1, backoffMillis);
            eventBus.publish(OrderRetriedEvent.now(order.getId(), attempt + 1, backoffMillis));
            orderMetricsService.recordRetried();
            pause(backoffMillis);
        }
    }

    private ExecutionResult result(Order order, boolean successful, String outcome) {
        return new ExecutionResult(
                order.getId(),
                OrderStatus.ACCEPTED,
                order.getStatus(),
                successful,
                Thread.currentThread().getName(),
                outcome,
                Instant.now()
        );
    }

    private void pauseIfConfigured() {
        pause(executionProperties.processingDelayMillis());
    }

    private void pause(long delayMillis) {
        if (delayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Order processing interrupted", ex);
        }
    }

    private double activeWorkerCount() {
        return workerFutures.stream()
                .filter(workerFuture -> !workerFuture.isDone())
                .count();
    }
}
