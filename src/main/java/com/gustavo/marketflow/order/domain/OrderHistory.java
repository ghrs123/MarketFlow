package com.gustavo.marketflow.order.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable history event stored for each order lifecycle transition.
 */
public record OrderHistory(
        UUID id,
        UUID orderId,
        String eventType,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        Instant occurredAt,
        String payloadJson,
        Instant createdAt
) {
}
