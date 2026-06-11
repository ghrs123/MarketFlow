package com.gustavo.marketflow.monitoring.infrastructure;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.gustavo.marketflow.execution.application.ExecutionStats;
import com.gustavo.marketflow.execution.application.OrderProcessingEngine;

/**
 * Exposes worker availability without treating a deliberate engine stop as application failure.
 */
@Component("processingEngineHealthIndicator")
public class ProcessingEngineHealthIndicator implements HealthIndicator {

    private final OrderProcessingEngine orderProcessingEngine;

    public ProcessingEngineHealthIndicator(OrderProcessingEngine orderProcessingEngine) {
        this.orderProcessingEngine = orderProcessingEngine;
    }

    @Override
    public Health health() {
        ExecutionStats stats = orderProcessingEngine.getStats();
        return Health.up()
                .withDetail("running", stats.running())
                .withDetail("configuredWorkers", stats.configuredWorkers())
                .withDetail("activeWorkers", stats.activeWorkers())
                .build();
    }
}
