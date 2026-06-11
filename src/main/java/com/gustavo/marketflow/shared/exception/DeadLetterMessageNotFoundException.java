package com.gustavo.marketflow.shared.exception;

import java.util.UUID;

/**
 * Raised when a requested order has no message in the in-memory DLQ.
 */
public class DeadLetterMessageNotFoundException extends RuntimeException {

    public DeadLetterMessageNotFoundException(UUID orderId) {
        super("Dead-letter message not found for order " + orderId);
    }
}
