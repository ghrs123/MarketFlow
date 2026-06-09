package com.gustavo.marketflow.orderbook.application;

import com.gustavo.marketflow.orderbook.domain.OrderTask;

import java.time.Instant;
import java.util.List;

/**
 * Immutable application-level snapshot of the in-memory order book.
 */
public record OrderBookSnapshot(
        List<OrderTask> buyOrders,
        List<OrderTask> sellOrders,
        int buyCount,
        int sellCount,
        Instant timestamp
) {
}
