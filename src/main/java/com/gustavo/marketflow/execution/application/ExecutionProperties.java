package com.gustavo.marketflow.execution.application;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed configuration for the in-process execution engine.
 *
 * <p>Phase 5 introduces asynchronous workers and an internal queue, so the
 * relevant concurrency knobs must be externally configurable and validated
 * at startup instead of being scattered across hard-coded constants.</p>
 */
@Validated
@ConfigurationProperties(prefix = "marketflow.execution")
public record ExecutionProperties(
        @Min(1) @Max(32) int workerCount,
        @Min(1) @Max(10_000) int queueCapacity,
        @Min(0) @Max(60_000) int processingDelayMillis,
        @Min(1) @Max(10) int retryMaxAttempts,
        @Min(0) @Max(60_000) int retryInitialBackoffMillis,
        @Min(0) @Max(300_000) int retryMaxBackoffMillis
) {
}
