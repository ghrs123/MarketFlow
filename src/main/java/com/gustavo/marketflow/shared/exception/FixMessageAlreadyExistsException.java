package com.gustavo.marketflow.shared.exception;

import java.util.UUID;

/**
 * Thrown when an order already owns a generated simulated FIX message.
 */
public class FixMessageAlreadyExistsException extends RuntimeException {

    private final UUID orderId;

    public FixMessageAlreadyExistsException(UUID orderId) {
        super("FIX message already exists for order: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
