package com.gustavo.marketflow.orderbook.api;

import com.gustavo.marketflow.orderbook.domain.OrderTask;

import java.time.Instant;
import java.util.List;

/**
 * Snapshot of the in-memory order book grouped by side.
 */
public record OrderBookSnapshotResponse(
        List<OrderTaskResponse> buyOrders,
        List<OrderTaskResponse> sellOrders,
        int buyCount,
        int sellCount,
        Instant timestamp
) {

    public static OrderBookSnapshotResponse from(List<OrderTask> buyOrders,
                                                 List<OrderTask> sellOrders,
                                                 Instant timestamp) {
        return new OrderBookSnapshotResponse(
                buyOrders.stream().map(OrderTaskResponse::from).toList(),
                sellOrders.stream().map(OrderTaskResponse::from).toList(),
                buyOrders.size(),
                sellOrders.size(),
                timestamp
        );
    }
}
