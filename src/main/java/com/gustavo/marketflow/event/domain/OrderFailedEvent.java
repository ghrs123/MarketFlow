package com.gustavo.marketflow.event.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Signals a failed processing attempt without exposing internal exception data.
 */
public record OrderFailedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt,
        int attempt,
        String reason
) implements DomainEvent {

    public static OrderFailedEvent now(UUID orderId, int attempt, String reason) {
        return new OrderFailedEvent(UUID.randomUUID(), orderId, Instant.now(), attempt, reason);
    }

    @Override
    public String type() {
        return "ORDER_FAILED";
    }
}
