package com.gustavo.marketflow.fix.api;

import java.time.Instant;
import java.util.UUID;

import com.gustavo.marketflow.fix.domain.FixMessage;

/**
 * Public response for a persisted simulated FIX message.
 */
public record FixMessageResponse(
        UUID id,
        UUID orderId,
        String rawMessage,
        Instant createdAt,
        Instant updatedAt
) {

    public static FixMessageResponse from(FixMessage fixMessage) {
        return new FixMessageResponse(
                fixMessage.id(),
                fixMessage.orderId(),
                fixMessage.rawMessage(),
                fixMessage.createdAt(),
                fixMessage.updatedAt()
        );
    }
}
