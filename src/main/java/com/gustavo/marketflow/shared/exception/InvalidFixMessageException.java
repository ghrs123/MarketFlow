package com.gustavo.marketflow.shared.exception;

/**
 * Thrown when a caller-provided simulated FIX message cannot be parsed safely.
 */
public class InvalidFixMessageException extends RuntimeException {

    public InvalidFixMessageException(String message) {
        super(message);
    }
}
