package com.gustavo.marketflow.order.api;

import com.gustavo.marketflow.order.domain.OrderHistory;
import com.gustavo.marketflow.order.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * API contract for order history entries.
 */
public record OrderHistoryResponse(
        UUID id,
        UUID orderId,
        String eventType,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        Instant occurredAt,
        String payloadJson,
        Instant createdAt
) {

    public static OrderHistoryResponse from(OrderHistory history) {
        return new OrderHistoryResponse(
                history.id(),
                history.orderId(),
                history.eventType(),
                history.previousStatus(),
                history.newStatus(),
                history.occurredAt(),
                history.payloadJson(),
                history.createdAt()
        );
    }
}
