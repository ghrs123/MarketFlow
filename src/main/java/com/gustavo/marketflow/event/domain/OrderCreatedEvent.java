package com.gustavo.marketflow.event.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Signals that an order was persisted successfully.
 */
public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) implements DomainEvent {

    public static OrderCreatedEvent now(UUID orderId) {
        return new OrderCreatedEvent(UUID.randomUUID(), orderId, Instant.now());
    }

    @Override
    public String type() {
        return "ORDER_CREATED";
    }
}
