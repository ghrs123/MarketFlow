package com.gustavo.marketflow.shared.exception;

import java.util.UUID;

/**
 * Thrown when a persisted order is added twice to the in-memory order book.
 *
 * <p>The book keeps a unique entry per order id in this phase, so duplicate
 * insertion attempts are treated as a conflict rather than a no-op.</p>
 */
public class OrderAlreadyInBookException extends RuntimeException {

    private final UUID orderId;

    public OrderAlreadyInBookException(UUID orderId) {
        super("Order already present in order book: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
