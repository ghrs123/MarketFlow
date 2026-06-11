package com.gustavo.marketflow.execution.domain;

/**
 * Immutable retry policy with capped exponential backoff.
 */
public record RetryPolicy(
        int maxAttempts,
        long initialBackoffMillis,
        long maxBackoffMillis
) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (initialBackoffMillis < 0 || maxBackoffMillis < initialBackoffMillis) {
            throw new IllegalArgumentException("invalid retry backoff range");
        }
    }

    public boolean canRetry(int completedAttempts) {
        return completedAttempts < maxAttempts;
    }

    public long backoffMillisFor(int completedAttempts) {
        if (completedAttempts < 1 || initialBackoffMillis == 0) {
            return 0;
        }
        int exponent = Math.min(completedAttempts - 1, 30);
        long multiplier = 1L << exponent;
        long calculated;
        try {
            calculated = Math.multiplyExact(initialBackoffMillis, multiplier);
        } catch (ArithmeticException ex) {
            calculated = Long.MAX_VALUE;
        }
        return Math.min(calculated, maxBackoffMillis);
    }
}
