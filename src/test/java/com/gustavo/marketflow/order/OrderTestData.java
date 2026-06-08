package com.gustavo.marketflow.order;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Provides complete and valid order fixtures shared across test layers.
 *
 * <p>Each factory creates a new identity while keeping business values and
 * timestamps deterministic, preventing accidental coupling between tests.</p>
 */
public final class OrderTestData {

    private static final String DEFAULT_CLIENT_ID = "C001";
    private static final String DEFAULT_SYMBOL = "AAPL";
    private static final OrderSide DEFAULT_SIDE = OrderSide.BUY;
    private static final BigDecimal DEFAULT_QUANTITY = new BigDecimal("100.00000000");
    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("150.25000000");
    private static final Instant DEFAULT_TIMESTAMP = Instant.parse("2026-01-15T10:30:00Z");

    private OrderTestData() {
    }

    /**
     * Creates a valid order in its initial status.
     *
     * @return a complete order fixture
     */
    public static Order valid() {
        return build(DEFAULT_CLIENT_ID, DEFAULT_SYMBOL, OrderStatus.NEW);
    }

    /**
     * Creates a valid order with the requested lifecycle status.
     *
     * @param status lifecycle status to assign
     * @return a complete order fixture
     */
    public static Order withStatus(OrderStatus status) {
        return build(DEFAULT_CLIENT_ID, DEFAULT_SYMBOL, status);
    }

    /**
     * Creates a valid order owned by the requested client.
     *
     * @param clientId client identifier to assign
     * @return a complete order fixture
     */
    public static Order withClientId(String clientId) {
        return build(clientId, DEFAULT_SYMBOL, OrderStatus.NEW);
    }

    /**
     * Creates a valid order for the requested market symbol.
     *
     * @param symbol market symbol to assign
     * @return a complete order fixture
     */
    public static Order withSymbol(String symbol) {
        return build(DEFAULT_CLIENT_ID, symbol, OrderStatus.NEW);
    }

    private static Order build(String clientId, String symbol, OrderStatus status) {
        return new Order(
                UUID.randomUUID(),
                clientId,
                symbol,
                DEFAULT_SIDE,
                DEFAULT_QUANTITY,
                DEFAULT_PRICE,
                status,
                DEFAULT_TIMESTAMP,
                DEFAULT_TIMESTAMP
        );
    }
}
