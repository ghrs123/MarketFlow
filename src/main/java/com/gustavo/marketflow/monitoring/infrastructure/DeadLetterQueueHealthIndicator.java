package com.gustavo.marketflow.monitoring.infrastructure;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.gustavo.marketflow.execution.application.DeadLetterQueue;

/**
 * Surfaces dead-letter accumulation while keeping the dependency available for reprocessing.
 */
@Component("deadLetterQueueHealthIndicator")
public class DeadLetterQueueHealthIndicator implements HealthIndicator {

    private final DeadLetterQueue deadLetterQueue;

    public DeadLetterQueueHealthIndicator(DeadLetterQueue deadLetterQueue) {
        this.deadLetterQueue = deadLetterQueue;
    }

    @Override
    public Health health() {
        int size = deadLetterQueue.size();
        return Health.up()
                .withDetail("size", size)
                .withDetail("warning", size > 0)
                .build();
    }
}
