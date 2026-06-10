package com.gustavo.marketflow.execution.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderQueueTest {

    @Test
    void enqueue_availableCapacity_returnsEnqueuedAndMakesOrderPollable() throws Exception {
        OrderQueue orderQueue = new OrderQueue(1);
        QueuedOrder queuedOrder = queuedOrder(UUID.randomUUID());

        OrderEnqueueStatus status = orderQueue.enqueue(queuedOrder);

        assertThat(status).isEqualTo(OrderEnqueueStatus.ENQUEUED);
        assertThat(orderQueue.poll(Duration.ZERO)).contains(queuedOrder);
    }

    @Test
    void enqueue_sameOrderTwice_returnsDuplicate() {
        OrderQueue orderQueue = new OrderQueue(2);
        UUID orderId = UUID.randomUUID();

        assertThat(orderQueue.enqueue(queuedOrder(orderId))).isEqualTo(OrderEnqueueStatus.ENQUEUED);
        assertThat(orderQueue.enqueue(queuedOrder(orderId))).isEqualTo(OrderEnqueueStatus.DUPLICATE);
        assertThat(orderQueue.size()).isEqualTo(1);
    }

    @Test
    void enqueue_whenCapacityIsExhausted_returnsFull() {
        OrderQueue orderQueue = new OrderQueue(1);

        assertThat(orderQueue.enqueue(queuedOrder(UUID.randomUUID()))).isEqualTo(OrderEnqueueStatus.ENQUEUED);
        assertThat(orderQueue.enqueue(queuedOrder(UUID.randomUUID()))).isEqualTo(OrderEnqueueStatus.FULL);
        assertThat(orderQueue.size()).isEqualTo(1);
    }

    @Test
    void markProcessed_releasesDuplicateGuard() {
        OrderQueue orderQueue = new OrderQueue(2);
        UUID orderId = UUID.randomUUID();
        assertThat(orderQueue.enqueue(queuedOrder(orderId))).isEqualTo(OrderEnqueueStatus.ENQUEUED);

        orderQueue.markProcessed(orderId);

        assertThat(orderQueue.enqueue(queuedOrder(orderId))).isEqualTo(OrderEnqueueStatus.ENQUEUED);
    }

    private QueuedOrder queuedOrder(UUID orderId) {
        return new QueuedOrder(orderId, Map.of("correlationId", "queue-test"), Instant.now());
    }
}
