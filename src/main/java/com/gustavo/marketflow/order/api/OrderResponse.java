package com.gustavo.marketflow.order.api;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Outbound DTO returned by the orders API.
 *
 * <p>Decoupled from the {@link Order} domain aggregate so that internal
 * refactors (renaming fields, splitting the aggregate, adding internal
 * fields) cannot break the public REST contract.</p>
 *
 * <p>The static {@link #from(Order)} factory centralises the mapping so it
 * is not scattered across controllers.</p>
 */
public record OrderResponse(
        UUID id,
        String clientId,
        String symbol,
        OrderSide side,
        BigDecimal quantity,
        BigDecimal price,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getClientId(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
