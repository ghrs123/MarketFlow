package com.gustavo.marketflow.monitoring.api;

import java.time.Instant;

/**
 * Stable operational snapshot independent from the Actuator response contract.
 */
public record MonitoringSummaryResponse(
        String service,
        boolean engineRunning,
        int configuredWorkers,
        int activeWorkers,
        int queueSize,
        int deadLetterQueueSize,
        long totalQueued,
        long totalProcessed,
        long totalSucceeded,
        long totalFailed,
        Instant timestamp
) {
}
