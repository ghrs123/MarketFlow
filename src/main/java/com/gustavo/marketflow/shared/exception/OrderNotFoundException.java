package com.gustavo.marketflow.shared.exception;

import java.util.UUID;

/**
 * Thrown when an order lookup by id does not match any stored order.
 *
 * <p>Extends {@link RuntimeException} so it does not pollute service
 * signatures with checked exceptions. Translated by
 * {@code GlobalExceptionHandler} into a 404 Not Found response that
 * follows RFC 7807.</p>
 */
public class OrderNotFoundException extends RuntimeException {

    private final UUID orderId;

    public OrderNotFoundException(UUID orderId) {
        super("Order not found: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
