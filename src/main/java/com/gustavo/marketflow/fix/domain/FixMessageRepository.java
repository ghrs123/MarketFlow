package com.gustavo.marketflow.fix.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for generated simulated FIX messages.
 */
public interface FixMessageRepository {

    FixMessage save(FixMessage fixMessage);

    Optional<FixMessage> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);
}
