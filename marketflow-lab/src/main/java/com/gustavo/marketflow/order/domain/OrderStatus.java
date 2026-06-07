package com.gustavo.marketflow.order.domain;

/**
 * Lifecycle status of an order.
 *
 * <p>In Phase 1 every newly created order is {@link #NEW}. Later phases
 * introduce the processing engine that transitions orders through
 * {@link #ACCEPTED}, {@link #EXECUTED}, {@link #REJECTED} or
 * {@link #FAILED}.</p>
 */
public enum OrderStatus {
    NEW,
    ACCEPTED,
    EXECUTED,
    REJECTED,
    FAILED
}
