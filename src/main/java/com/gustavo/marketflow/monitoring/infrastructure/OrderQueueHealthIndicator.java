package com.gustavo.marketflow.monitoring.infrastructure;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.gustavo.marketflow.execution.application.ExecutionProperties;
import com.gustavo.marketflow.execution.domain.OrderQueue;

/**
 * Reports order queue saturation because a full bounded queue rejects new processing work.
 */
@Component("orderQueueHealthIndicator")
public class OrderQueueHealthIndicator implements HealthIndicator {

    private final OrderQueue orderQueue;
    private final ExecutionProperties executionProperties;

    public OrderQueueHealthIndicator(OrderQueue orderQueue, ExecutionProperties executionProperties) {
        this.orderQueue = orderQueue;
        this.executionProperties = executionProperties;
    }

    @Override
    public Health health() {
        int size = orderQueue.size();
        int capacity = executionProperties.queueCapacity();
        Health.Builder builder = size >= capacity ? Health.outOfService() : Health.up();
        return builder
                .withDetail("size", size)
                .withDetail("capacity", capacity)
                .withDetail("utilizationPercent", percentage(size, capacity))
                .build();
    }

    private int percentage(int size, int capacity) {
        return (int) Math.round((size * 100.0) / capacity);
    }
}
