package com.gustavo.marketflow.event.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Signals that an order entered the internal execution queue.
 */
public record OrderQueuedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) implements DomainEvent {

    public static OrderQueuedEvent now(UUID orderId) {
        return new OrderQueuedEvent(UUID.randomUUID(), orderId, Instant.now());
    }

    @Override
    public String type() {
        return "ORDER_QUEUED";
    }
}
