package com.gustavo.marketflow.execution.application;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderHistory;
import com.gustavo.marketflow.order.domain.OrderHistoryRepository;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.resilience.application.BrokerExecutionResult;
import com.gustavo.marketflow.resilience.infrastructure.BrokerClient;
import com.gustavo.marketflow.resilience.infrastructure.NotificationClient;
import com.gustavo.marketflow.resilience.infrastructure.TransientExternalServiceException;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import com.gustavo.marketflow.shared.exception.OrderNotQueueableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(OrderExecutionService.class);

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final BrokerClient brokerClient;
    private final NotificationClient notificationClient;

    public OrderExecutionService(OrderRepository orderRepository,
                                 OrderHistoryRepository orderHistoryRepository,
                                 BrokerClient brokerClient,
                                 NotificationClient notificationClient) {
        this.orderRepository = orderRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.brokerClient = brokerClient;
        this.notificationClient = notificationClient;
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

    /**
     * Calls the simulated broker behind a circuit breaker without changing local order state.
     *
     * <p>Keeping the remote call outside a database transaction prevents a slow dependency
     * from holding a JDBC connection and database locks.</p>
     */
    @CircuitBreaker(name = "broker", fallbackMethod = "brokerFallback")
    public BrokerExecutionResult executeWithBroker(UUID orderId) {
        Order order = requireExisting(orderId);
        String brokerReference = brokerClient.execute(order);
        notificationClient.notifyBrokerAccepted(orderId, brokerReference);
        log.info("Order {} accepted by simulated broker reference={}", orderId, brokerReference);
        return new BrokerExecutionResult(
                orderId,
                "BROKER_ACCEPTED",
                brokerReference,
                false,
                Instant.now()
        );
    }

    private Order requireExisting(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private BrokerExecutionResult brokerFallback(UUID orderId,
                                                 TransientExternalServiceException cause) {
        return degradedBrokerResult(orderId, cause);
    }

    private BrokerExecutionResult brokerFallback(UUID orderId,
                                                 CallNotPermittedException cause) {
        return degradedBrokerResult(orderId, cause);
    }

    private BrokerExecutionResult degradedBrokerResult(UUID orderId, RuntimeException cause) {
        log.warn("Broker fallback used for order {} cause={}",
                orderId, cause.getClass().getSimpleName());
        return new BrokerExecutionResult(
                orderId,
                "PENDING_BROKER_RECOVERY",
                null,
                true,
                Instant.now()
        );
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
