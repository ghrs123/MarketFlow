package com.gustavo.marketflow.monitoring.infrastructure;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import com.gustavo.marketflow.execution.application.ExecutionProperties;
import com.gustavo.marketflow.execution.domain.OrderQueue;
import com.gustavo.marketflow.execution.domain.QueuedOrder;

import static org.assertj.core.api.Assertions.assertThat;

class OrderQueueHealthIndicatorTest {

    @Test
    void health_availableCapacity_reportsUp() {
        OrderQueue queue = new OrderQueue(2);
        OrderQueueHealthIndicator indicator = new OrderQueueHealthIndicator(queue, properties(2));

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void health_fullQueue_reportsOutOfService() {
        OrderQueue queue = new OrderQueue(1);
        queue.enqueue(QueuedOrder.capture(UUID.randomUUID()));
        OrderQueueHealthIndicator indicator = new OrderQueueHealthIndicator(queue, properties(1));

        assertThat(indicator.health().getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
        assertThat(indicator.health().getDetails()).containsAllEntriesOf(Map.of(
                "size", 1,
                "capacity", 1,
                "utilizationPercent", 100
        ));
    }

    private ExecutionProperties properties(int capacity) {
        return new ExecutionProperties(1, capacity, 0, 3, 0, 0);
    }
}
