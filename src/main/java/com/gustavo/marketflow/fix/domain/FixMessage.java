package com.gustavo.marketflow.fix.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Persisted representation of a simulated FIX message generated for an order.
 *
 * <p>The raw payload is intentionally stored unchanged so the educational API
 * can demonstrate parsing and tag explanation without claiming wire-level FIX
 * compatibility.</p>
 */
public record FixMessage(
        UUID id,
        UUID orderId,
        String rawMessage,
        Instant createdAt,
        Instant updatedAt
) {

    public FixMessage {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(rawMessage, "rawMessage");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }
}
