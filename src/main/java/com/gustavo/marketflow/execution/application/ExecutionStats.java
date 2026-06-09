package com.gustavo.marketflow.execution.application;

import java.time.Instant;

/**
 * Snapshot of execution-engine state returned to the API layer.
 */
public record ExecutionStats(
        boolean running,
        int configuredWorkers,
        int activeWorkers,
        int queueSize,
        long totalQueued,
        long totalProcessed,
        long totalSucceeded,
        long totalFailed,
        Instant timestamp
) {
}
