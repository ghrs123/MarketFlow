package com.gustavo.marketflow.event.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Signals that the simulated FIX representation of an order was generated.
 */
public record FixMessageGeneratedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) implements DomainEvent {

    public static FixMessageGeneratedEvent now(UUID orderId) {
        return new FixMessageGeneratedEvent(UUID.randomUUID(), orderId, Instant.now());
    }

    @Override
    public String type() {
        return "FIX_MESSAGE_GENERATED";
    }
}
