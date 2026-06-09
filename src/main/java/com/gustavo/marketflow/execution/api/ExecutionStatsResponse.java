package com.gustavo.marketflow.execution.api;

import com.gustavo.marketflow.execution.application.ExecutionStats;

import java.time.Instant;

/**
 * Public response DTO for execution-engine statistics.
 */
public record ExecutionStatsResponse(
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

    public static ExecutionStatsResponse from(ExecutionStats executionStats) {
        return new ExecutionStatsResponse(
                executionStats.running(),
                executionStats.configuredWorkers(),
                executionStats.activeWorkers(),
                executionStats.queueSize(),
                executionStats.totalQueued(),
                executionStats.totalProcessed(),
                executionStats.totalSucceeded(),
                executionStats.totalFailed(),
                executionStats.timestamp()
        );
    }
}
