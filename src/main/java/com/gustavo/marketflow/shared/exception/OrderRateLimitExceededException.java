package com.gustavo.marketflow.shared.exception;

/**
 * Signals that order creation exceeded the configured request budget.
 */
public class OrderRateLimitExceededException extends RuntimeException {

    public OrderRateLimitExceededException() {
        super("Order creation rate limit exceeded");
    }
}
