package com.gustavo.marketflow.orderbook.domain;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable in-memory representation of an order inside the order book.
 *
 * <p>The order book does not own persistence in this phase. It operates on a
 * read-model derived directly from the existing {@link Order} aggregate so the
 * in-memory structures can focus on ordering and lookup behaviour.</p>
 */
public record OrderTask(
        UUID orderId,
        String clientId,
        String symbol,
        OrderSide side,
        BigDecimal quantity,
        BigDecimal price,
        OrderStatus status,
        Instant createdAt
) {

    /**
     * Creates an in-memory order-book item from a persisted order aggregate.
     *
     * @param order persisted order used as the source of truth
     * @return immutable task stored inside the in-memory order book
     */
    public static OrderTask from(Order order) {
        return new OrderTask(
                order.getId(),
                order.getClientId(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
