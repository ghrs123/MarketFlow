package com.gustavo.marketflow.learning.concurrency;

/**
 * Result snapshot for the concurrency counter demos.
 */
public record RaceConditionDemoResult(
        int expected,
        int actual,
        String strategy
) {
}
