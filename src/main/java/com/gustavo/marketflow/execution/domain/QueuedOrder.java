package com.gustavo.marketflow.execution.domain;

import org.slf4j.MDC;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable queue payload for the in-process execution engine.
 *
 * <p>The queue carries the order identifier plus captured MDC state so
 * correlation information survives the hand-off from the HTTP thread to the
 * worker thread.</p>
 */
public record QueuedOrder(
        UUID orderId,
        Map<String, String> mdcContext,
        Instant queuedAt
) {

    public static QueuedOrder capture(UUID orderId) {
        return new QueuedOrder(orderId, MDC.getCopyOfContextMap(), Instant.now());
    }
}
