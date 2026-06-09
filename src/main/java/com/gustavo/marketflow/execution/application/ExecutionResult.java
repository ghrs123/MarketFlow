package com.gustavo.marketflow.execution.application;

import com.gustavo.marketflow.order.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable result of one asynchronous order-processing attempt.
 */
public record ExecutionResult(
        UUID orderId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        boolean success,
        String workerName,
        String outcome,
        Instant processedAt
) {
}
