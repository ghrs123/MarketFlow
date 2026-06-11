package com.gustavo.marketflow.shared.exception;

import java.util.UUID;

/**
 * Thrown when an existing order does not yet have a simulated FIX message.
 */
public class FixMessageNotFoundException extends RuntimeException {

    private final UUID orderId;

    public FixMessageNotFoundException(UUID orderId) {
        super("FIX message not found for order: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
