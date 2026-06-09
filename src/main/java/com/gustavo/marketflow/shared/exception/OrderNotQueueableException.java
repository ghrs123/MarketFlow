package com.gustavo.marketflow.shared.exception;

import com.gustavo.marketflow.order.domain.OrderStatus;

import java.util.UUID;

/**
 * Thrown when an order is not in a state that can enter the processing queue.
 */
public class OrderNotQueueableException extends RuntimeException {

    private final UUID orderId;
    private final OrderStatus currentStatus;

    public OrderNotQueueableException(UUID orderId, OrderStatus currentStatus) {
        super("Order is not queueable in status " + currentStatus + ": " + orderId);
        this.orderId = orderId;
        this.currentStatus = currentStatus;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }
}
