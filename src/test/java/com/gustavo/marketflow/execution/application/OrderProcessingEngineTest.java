package com.gustavo.marketflow.execution.application;

import com.gustavo.marketflow.execution.domain.OrderEnqueueStatus;
import com.gustavo.marketflow.execution.domain.OrderQueue;
import com.gustavo.marketflow.execution.domain.QueuedOrder;
import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.application.OrderApplicationService;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.shared.exception.OrderAlreadyQueuedException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderProcessingEngineTest {

    @Mock
    private OrderApplicationService orderApplicationService;

    @Mock
    private OrderExecutionService orderExecutionService;

    @Mock
    private OrderQueue orderQueue;

    private ExecutorService executorService;

    @AfterEach
    void tearDown() throws Exception {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
        MDC.clear();
    }

    @Test
    void enqueue_happyPath_recordsQueueing() {
        Order order = OrderTestData.valid();
        OrderProcessingEngine engine = newEngine(orderQueue, 1);
        when(orderExecutionService.requireQueueable(order.getId())).thenReturn(order);
        when(orderQueue.enqueue(org.mockito.ArgumentMatchers.any(QueuedOrder.class))).thenReturn(OrderEnqueueStatus.ENQUEUED);

        engine.enqueue(order.getId());

        verify(orderExecutionService).requireQueueable(order.getId());
        verify(orderQueue).enqueue(org.mockito.ArgumentMatchers.any(QueuedOrder.class));
        verify(orderExecutionService).recordQueued(order.getId());
    }

    @Test
    void enqueue_duplicate_throwsConflict() {
        Order order = OrderTestData.valid();
        OrderProcessingEngine engine = newEngine(orderQueue, 1);
        when(orderExecutionService.requireQueueable(order.getId())).thenReturn(order);
        when(orderQueue.enqueue(org.mockito.ArgumentMatchers.any(QueuedOrder.class))).thenReturn(OrderEnqueueStatus.DUPLICATE);

        assertThatThrownBy(() -> engine.enqueue(order.getId()))
                .isInstanceOf(OrderAlreadyQueuedException.class)
                .hasMessageContaining(order.getId().toString());
    }

    @Test
    void handleQueuedOrder_success_updatesStatsAndReleasesQueue() {
        UUID orderId = UUID.randomUUID();
        Order acceptedOrder = order(orderId, "AAPL", OrderStatus.ACCEPTED);
        Order executedOrder = order(orderId, "AAPL", OrderStatus.EXECUTED);
        OrderProcessingEngine engine = newEngine(orderQueue, 1);
        when(orderExecutionService.markAccepted(orderId)).thenReturn(acceptedOrder);
        when(orderExecutionService.markExecuted(orderId)).thenReturn(executedOrder);

        engine.handleQueuedOrder(new QueuedOrder(orderId, Map.of("correlationId", "test"), Instant.now()));

        ExecutionStats stats = engine.getStats();
        assertThat(stats.totalProcessed()).isEqualTo(1);
        assertThat(stats.totalSucceeded()).isEqualTo(1);
        assertThat(stats.totalFailed()).isZero();
        verify(orderQueue).markProcessed(orderId);
    }

    @Test
    void handleQueuedOrder_failure_updatesStatsAndReleasesQueue() {
        UUID orderId = UUID.randomUUID();
        Order acceptedOrder = order(orderId, "FAIL", OrderStatus.ACCEPTED);
        Order failedOrder = order(orderId, "FAIL", OrderStatus.FAILED);
        OrderProcessingEngine engine = newEngine(orderQueue, 1);
        when(orderExecutionService.markAccepted(orderId)).thenReturn(acceptedOrder);
        when(orderExecutionService.markFailed(orderId, "Simulated processing failure")).thenReturn(failedOrder);

        engine.handleQueuedOrder(new QueuedOrder(orderId, Map.of("correlationId", "test"), Instant.now()));

        ExecutionStats stats = engine.getStats();
        assertThat(stats.totalProcessed()).isEqualTo(1);
        assertThat(stats.totalSucceeded()).isZero();
        assertThat(stats.totalFailed()).isEqualTo(1);
        verify(orderQueue).markProcessed(orderId);
    }

    @Test
    void startThenStop_updatesRunningState() {
        OrderQueue realOrderQueue = new OrderQueue(10);
        OrderProcessingEngine engine = newEngine(realOrderQueue, 1);

        engine.start();

        assertThat(engine.getStats().running()).isTrue();
        assertThat(engine.getStats().activeWorkers()).isEqualTo(1);

        engine.stop();

        assertThat(engine.getStats().running()).isFalse();
    }

    private OrderProcessingEngine newEngine(OrderQueue queue, int workerCount) {
        executorService = Executors.newFixedThreadPool(workerCount, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("engine-test-" + thread.threadId());
            return thread;
        });
        return new OrderProcessingEngine(
                orderApplicationService,
                orderExecutionService,
                queue,
                new ExecutionProperties(workerCount, 10, 0),
                executorService,
                new SimpleMeterRegistry()
        );
    }

    private Order order(UUID orderId, String symbol, OrderStatus status) {
        Instant now = Instant.parse("2026-01-15T10:30:00Z");
        return new Order(
                orderId,
                "C001",
                symbol,
                OrderSide.BUY,
                new BigDecimal("10.00000000"),
                new BigDecimal("150.25000000"),
                status,
                now,
                now
        );
    }
}
