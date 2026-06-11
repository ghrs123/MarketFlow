package com.gustavo.marketflow.resilience.infrastructure;

/**
 * Signals a simulated dependency failure that is eligible for retry.
 */
public class TransientExternalServiceException extends RuntimeException {

    public TransientExternalServiceException(String message) {
        super(message);
    }
}
