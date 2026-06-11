package com.gustavo.marketflow.event.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Signals that another execution attempt will be made after a backoff.
 */
public record OrderRetriedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt,
        int nextAttempt,
        long backoffMillis
) implements DomainEvent {

    public static OrderRetriedEvent now(UUID orderId, int nextAttempt, long backoffMillis) {
        return new OrderRetriedEvent(UUID.randomUUID(), orderId, Instant.now(), nextAttempt, backoffMillis);
    }

    @Override
    public String type() {
        return "ORDER_RETRIED";
    }
}
