package com.gustavo.marketflow.execution.application;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderHistory;
import com.gustavo.marketflow.order.domain.OrderHistoryRepository;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import com.gustavo.marketflow.shared.exception.OrderNotQueueableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Transactional order-state transitions used by the async execution engine.
 *
 * <p>The processing engine itself is not transactional because it owns queue
 * lifecycle and worker threads. Actual order state changes are isolated here
 * so each transition persists atomically with its history entry.</p>
 */
@Service
public class OrderExecutionService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    public OrderExecutionService(OrderRepository orderRepository,
                                 OrderHistoryRepository orderHistoryRepository) {
        this.orderRepository = orderRepository;
        this.orderHistoryRepository = orderHistoryRepository;
    }

    @Transactional(readOnly = true)
    public Order requireQueueable(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (order.getStatus() != OrderStatus.NEW) {
            throw new OrderNotQueueableException(orderId, order.getStatus());
        }
        return order;
    }

    @Transactional
    public void recordQueued(UUID orderId) {
        Order order = requireExisting(orderId);
        saveHistory(order, "ORDER_QUEUED", order.getStatus(), order.getStatus(), null);
    }

    @Transactional
    public Order markAccepted(UUID orderId) {
        Order order = requireExisting(orderId);
        OrderStatus previousStatus = order.getStatus();
        order.changeStatus(OrderStatus.ACCEPTED);
        Order updatedOrder = orderRepository.updateStatus(order.getId(), order.getStatus(), order.getUpdatedAt());
        saveHistory(updatedOrder, "ORDER_ACCEPTED", previousStatus, updatedOrder.getStatus(), null);
        return updatedOrder;
    }

    @Transactional
    public Order markExecuted(UUID orderId) {
        Order order = requireExisting(orderId);
        OrderStatus previousStatus = order.getStatus();
        order.changeStatus(OrderStatus.EXECUTED);
        Order updatedOrder = orderRepository.updateStatus(order.getId(), order.getStatus(), order.getUpdatedAt());
        saveHistory(updatedOrder, "ORDER_EXECUTED", previousStatus, updatedOrder.getStatus(), null);
        return updatedOrder;
    }

    @Transactional
    public Order markFailed(UUID orderId, String outcome) {
        Order order = requireExisting(orderId);
        OrderStatus previousStatus = order.getStatus();
        order.changeStatus(OrderStatus.FAILED);
        Order updatedOrder = orderRepository.updateStatus(order.getId(), order.getStatus(), order.getUpdatedAt());
        saveHistory(updatedOrder, "ORDER_FAILED", previousStatus, updatedOrder.getStatus(), outcome);
        return updatedOrder;
    }

    @Transactional
    public Order resetForReprocessing(UUID orderId) {
        Order order = requireExisting(orderId);
        OrderStatus previousStatus = order.getStatus();
        order.changeStatus(OrderStatus.NEW);
        Order updatedOrder = orderRepository.updateStatus(order.getId(), order.getStatus(), order.getUpdatedAt());
        saveHistory(updatedOrder, "ORDER_DLQ_REPROCESSED", previousStatus, updatedOrder.getStatus(), null);
        return updatedOrder;
    }

    @Transactional
    public void recordRetry(UUID orderId, int attempt, long backoffMillis) {
        Order order = requireExisting(orderId);
        String payload = "{\"attempt\":" + attempt + ",\"backoffMillis\":" + backoffMillis + "}";
        saveHistory(order, "ORDER_RETRIED", order.getStatus(), order.getStatus(), payload);
    }

    private Order requireExisting(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void saveHistory(Order order,
                             String eventType,
                             OrderStatus previousStatus,
                             OrderStatus newStatus,
                             String payloadJson) {
        Instant now = Instant.now();
        orderHistoryRepository.save(new OrderHistory(
                UUID.randomUUID(),
                order.getId(),
                eventType,
                previousStatus,
                newStatus,
                now,
                payloadJson,
                now
        ));
    }
}
