package com.gustavo.marketflow.monitoring.infrastructure;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import com.gustavo.marketflow.execution.application.DeadLetterQueue;
import com.gustavo.marketflow.execution.domain.DeadLetterMessage;

import static org.assertj.core.api.Assertions.assertThat;

class DeadLetterQueueHealthIndicatorTest {

    @Test
    void health_nonEmptyQueue_reportsWarningWithoutTakingServiceDown() {
        DeadLetterQueue queue = new DeadLetterQueue();
        queue.add(DeadLetterMessage.create(UUID.randomUUID(), "failure", 3, Map.of()));
        DeadLetterQueueHealthIndicator indicator = new DeadLetterQueueHealthIndicator(queue);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.health().getDetails())
                .containsEntry("size", 1)
                .containsEntry("warning", true);
    }
}
