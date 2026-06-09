package com.gustavo.marketflow.shared.exception;

/**
 * Thrown when the requested side of the in-memory order book has no entries.
 *
 * <p>The client asked for the best BUY or SELL, but there is no current head
 * item to return from that side.</p>
 */
public class EmptyOrderBookSideException extends RuntimeException {

    public EmptyOrderBookSideException(String side) {
        super("Order book side is empty: " + side);
    }
}
