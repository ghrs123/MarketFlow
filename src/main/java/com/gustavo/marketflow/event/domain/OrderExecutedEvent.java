package com.gustavo.marketflow.event.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Signals successful completion of order execution.
 */
public record OrderExecutedEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt
) implements DomainEvent {

    public static OrderExecutedEvent now(UUID orderId) {
        return new OrderExecutedEvent(UUID.randomUUID(), orderId, Instant.now());
    }

    @Override
    public String type() {
        return "ORDER_EXECUTED";
    }
}
