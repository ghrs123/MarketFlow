package com.gustavo.marketflow.resilience.infrastructure;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.resilience.application.ResilienceProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides deterministic transient failures so FIX retry behaviour is observable.
 */
@Component
public class FixGenerationAvailability {

    private static final String RETRY_SYMBOL = "FIX_RETRY";
    private static final String FAILURE_SYMBOL = "FIX_FAIL";

    private final ResilienceProperties properties;
    private final ConcurrentHashMap<UUID, AtomicInteger> attempts;

    public FixGenerationAvailability(ResilienceProperties properties) {
        this.properties = properties;
        this.attempts = new ConcurrentHashMap<>();
    }

    /**
     * Fails deterministically for learning symbols while leaving normal symbols untouched.
     */
    public void assertAvailable(Order order) {
        if (FAILURE_SYMBOL.equalsIgnoreCase(order.getSymbol())) {
            throw new TransientExternalServiceException("Simulated FIX dependency failure");
        }
        if (!RETRY_SYMBOL.equalsIgnoreCase(order.getSymbol())) {
            return;
        }

        int currentAttempt = attempts
                .computeIfAbsent(order.getId(), ignored -> new AtomicInteger())
                .incrementAndGet();
        if (currentAttempt <= properties.fixTransientFailures()) {
            throw new TransientExternalServiceException("Simulated transient FIX dependency failure");
        }
        attempts.remove(order.getId());
    }
}
