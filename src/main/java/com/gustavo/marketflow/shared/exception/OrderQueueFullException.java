package com.gustavo.marketflow.shared.exception;

import java.util.UUID;

/**
 * Thrown when the bounded in-memory execution queue cannot accept more work.
 */
public class OrderQueueFullException extends RuntimeException {

    private final UUID orderId;

    public OrderQueueFullException(UUID orderId) {
        super("Order queue is full for order: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
