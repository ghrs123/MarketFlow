package com.gustavo.marketflow.shared.exception;

import java.util.UUID;

/**
 * Thrown when an order is not present in the recent in-memory cache.
 *
 * <p>The recent-order cache is bounded and non-persistent, so cache misses are
 * expected and translated into a 404 at the API boundary.</p>
 */
public class OrderNotInCacheException extends RuntimeException {

    private final UUID orderId;

    public OrderNotInCacheException(UUID orderId) {
        super("Order not present in recent cache: " + orderId);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
