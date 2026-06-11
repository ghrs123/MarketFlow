package com.gustavo.marketflow.resilience.application;

import java.time.Instant;
import java.util.UUID;

/**
 * Public outcome of a broker execution attempt, including graceful degradation.
 */
public record BrokerExecutionResult(
        UUID orderId,
        String status,
        String brokerReference,
        boolean fallback,
        Instant processedAt
) {
}
