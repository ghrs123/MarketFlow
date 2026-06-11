package com.gustavo.marketflow.event.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Common contract for immutable events emitted by MarketFlow business flows.
 */
public interface DomainEvent {

    UUID eventId();

    UUID orderId();

    Instant occurredAt();

    @JsonProperty("type")
    String type();
}
