package com.gustavo.marketflow.orderbook.api;

import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.orderbook.domain.OrderTask;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Public representation of an order currently held in the in-memory book.
 */
public record OrderTaskResponse(
        UUID orderId,
        String clientId,
        String symbol,
        OrderSide side,
        BigDecimal quantity,
        BigDecimal price,
        OrderStatus status,
        Instant createdAt
) {

    public static OrderTaskResponse from(OrderTask orderTask) {
        return new OrderTaskResponse(
                orderTask.orderId(),
                orderTask.clientId(),
                orderTask.symbol(),
                orderTask.side(),
                orderTask.quantity(),
                orderTask.price(),
                orderTask.status(),
                orderTask.createdAt()
        );
    }
}
