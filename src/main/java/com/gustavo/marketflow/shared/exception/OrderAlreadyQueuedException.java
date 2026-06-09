package com.gustavo.marketflow.shared.exception;

import java.util.UUID;

/**
 * Thrown when an order is already waiting in the internal processing queue.
 */
public class OrderAlreadyQueuedException extends RuntimeException {

    private final UUID orderId;

    public OrderAlreadyQueuedException(UUID orderId) {
        super("Order already queued for processing: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
