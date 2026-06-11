package com.gustavo.marketflow.event.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Signals that an order request passed application validation.
 */
public record OrderValidatedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) implements DomainEvent {

    public static OrderValidatedEvent now(UUID orderId) {
        return new OrderValidatedEvent(UUID.randomUUID(), orderId, Instant.now());
    }

    @Override
    public String type() {
        return "ORDER_VALIDATED";
    }
}
