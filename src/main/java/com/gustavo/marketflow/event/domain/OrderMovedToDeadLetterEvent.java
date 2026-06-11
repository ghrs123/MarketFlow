package com.gustavo.marketflow.event.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Signals that retry exhaustion moved an order to the dead-letter queue.
 */
public record OrderMovedToDeadLetterEvent(
        UUID eventId,
        UUID orderId,
        Instant occurredAt,
        int attempts
) implements DomainEvent {

    public static OrderMovedToDeadLetterEvent now(UUID orderId, int attempts) {
        return new OrderMovedToDeadLetterEvent(UUID.randomUUID(), orderId, Instant.now(), attempts);
    }

    @Override
    public String type() {
        return "ORDER_MOVED_TO_DEAD_LETTER";
    }
}
