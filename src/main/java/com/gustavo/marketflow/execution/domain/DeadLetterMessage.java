package com.gustavo.marketflow.execution.domain;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable record of an order that exhausted all configured attempts.
 */
public record DeadLetterMessage(
        UUID id,
        UUID orderId,
        String reason,
        int attempts,
        Map<String, String> mdcContext,
        Instant createdAt
) {

    public DeadLetterMessage {
        mdcContext = mdcContext == null ? Map.of() : Map.copyOf(mdcContext);
    }

    public static DeadLetterMessage create(UUID orderId,
                                           String reason,
                                           int attempts,
                                           Map<String, String> mdcContext) {
        return new DeadLetterMessage(
                UUID.randomUUID(),
                orderId,
                reason,
                attempts,
                mdcContext,
                Instant.now()
        );
    }
}
