package com.gustavo.marketflow.execution.application;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe counters for the execution engine.
 *
 * <p>The engine updates these counters from multiple worker threads, so the
 * implementation uses atomics instead of synchronized integer mutation.</p>
 */
public class ExecutionStatistics {

    private final AtomicLong totalQueued;
    private final AtomicLong totalProcessed;
    private final AtomicLong totalSucceeded;
    private final AtomicLong totalFailed;

    public ExecutionStatistics() {
        this.totalQueued = new AtomicLong();
        this.totalProcessed = new AtomicLong();
        this.totalSucceeded = new AtomicLong();
        this.totalFailed = new AtomicLong();
    }

    public void recordQueued() {
        totalQueued.incrementAndGet();
    }

    public void recordProcessed(boolean success) {
        totalProcessed.incrementAndGet();
        if (success) {
            totalSucceeded.incrementAndGet();
            return;
        }
        totalFailed.incrementAndGet();
    }

    public ExecutionStats snapshot(boolean running,
                                   int configuredWorkers,
                                   int activeWorkers,
                                   int queueSize) {
        return new ExecutionStats(
                running,
                configuredWorkers,
                activeWorkers,
                queueSize,
                totalQueued.get(),
                totalProcessed.get(),
                totalSucceeded.get(),
                totalFailed.get(),
                Instant.now()
        );
    }
}
