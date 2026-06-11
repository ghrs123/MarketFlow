package com.gustavo.marketflow.execution.application;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe attempt registry for in-flight order processing.
 */
@Component
public class RetryRegistry {

    private final ConcurrentHashMap<UUID, AtomicInteger> attemptsByOrder;

    public RetryRegistry() {
        this.attemptsByOrder = new ConcurrentHashMap<>();
    }

    public int recordAttempt(UUID orderId) {
        return attemptsByOrder.computeIfAbsent(orderId, ignored -> new AtomicInteger())
                .incrementAndGet();
    }

    public int attemptsFor(UUID orderId) {
        AtomicInteger attempts = attemptsByOrder.get(orderId);
        return attempts == null ? 0 : attempts.get();
    }

    public void clear(UUID orderId) {
        attemptsByOrder.remove(orderId);
    }
}
