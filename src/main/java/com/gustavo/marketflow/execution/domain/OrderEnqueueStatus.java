package com.gustavo.marketflow.execution.domain;

/**
 * Result of attempting to enqueue an order for asynchronous processing.
 */
public enum OrderEnqueueStatus {
    ENQUEUED,
    DUPLICATE,
    FULL
}
