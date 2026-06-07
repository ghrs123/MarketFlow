package com.gustavo.marketflow.order.domain;

/**
 * Side of a financial order.
 *
 * <p>Modelled as a closed enum (rather than a String) so that invalid sides
 * cannot reach the domain layer and so that downstream consumers (matching
 * engine, FIX message generator) can switch on the exact set of cases.</p>
 */
public enum OrderSide {
    BUY,
    SELL
}
