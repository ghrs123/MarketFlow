package com.gustavo.marketflow.resilience.application;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Controls deterministic failure simulation without embedding timing policy in clients.
 */
@Validated
@ConfigurationProperties(prefix = "marketflow.resilience")
public record ResilienceProperties(
        @Min(1) long brokerTimeoutMillis,
        @Min(0) int fixTransientFailures
) {
}
