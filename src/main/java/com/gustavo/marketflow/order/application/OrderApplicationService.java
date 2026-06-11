package com.gustavo.marketflow.order.application;

import com.gustavo.marketflow.event.domain.OrderCreatedEvent;
import com.gustavo.marketflow.event.domain.OrderValidatedEvent;
import com.gustavo.marketflow.event.infrastructure.InMemoryEventBus;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderHistory;
import com.gustavo.marketflow.order.domain.OrderHistoryRepository;
import com.gustavo.marketflow.order.domain.OrderPage;
import com.gustavo.marketflow.order.domain.OrderRepository;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service that orchestrates order use cases.
 *
 * <p>Sits between the inbound REST adapter and the outbound persistence
 * port. Holds no state of its own and depends on
 * {@link OrderRepository} through constructor injection, which makes the
 * collaborator explicit, the class trivially testable with a fake
 * repository and the dependency graph validated at startup.</p>
 *
 * <p>The service deliberately speaks the domain language (it returns
 * {@link Order} aggregates) and never deals with HTTP, DTOs or
 * serialization. That mapping lives in the API layer.</p>
 */
@Service
public class OrderApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final IdempotencyRegistry idempotencyRegistry;
    private final InMemoryEventBus eventBus;

    public OrderApplicationService(OrderRepository orderRepository,
                                   OrderHistoryRepository orderHistoryRepository,
                                   IdempotencyRegistry idempotencyRegistry,
                                   InMemoryEventBus eventBus) {
        this.orderRepository = orderRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.idempotencyRegistry = idempotencyRegistry;
        this.eventBus = eventBus;
    }

    @Transactional
    public Order createOrder(String clientId,
                             String symbol,
                             OrderSide side,
                             BigDecimal quantity,
                             BigDecimal price) {
        return createOrder(clientId, symbol, side, quantity, price, UUID.randomUUID().toString());
    }

    /**
     * Creates an order once for a client-provided idempotency key and returns the existing order on replay.
     */
    @Transactional
    public Order createOrder(String clientId,
                             String symbol,
                             OrderSide side,
                             BigDecimal quantity,
                             BigDecimal price,
                             String idempotencyKey) {
        Order existingOrder = idempotencyRegistry.findExisting(idempotencyKey).orElse(null);
        if (existingOrder != null) {
            log.info("Duplicate order request resolved idempotently orderId={} idempotencyKey={}",
                    existingOrder.getId(), idempotencyKey);
            return existingOrder;
        }

        Order order = Order.createNew(clientId, symbol, side, quantity, price, idempotencyKey);
        eventBus.publish(OrderValidatedEvent.now(order.getId()));
        Order saved = orderRepository.save(order);
        orderHistoryRepository.save(new OrderHistory(
                UUID.randomUUID(),
                saved.getId(),
                "ORDER_CREATED",
                null,
                saved.getStatus(),
                Instant.now(),
                null,
                Instant.now()
        ));
        eventBus.publish(OrderCreatedEvent.now(saved.getId()));
        log.info("Order created id={} clientId={} symbol={} side={} qty={} price={}",
                saved.getId(), saved.getClientId(), saved.getSymbol(),
                saved.getSide(), saved.getQuantity(), saved.getPrice());
        return saved;
    }

    @Transactional(readOnly = true)
    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public OrderPage findByFilters(String clientId,
                                   OrderStatus status,
                                   int page,
                                   int size) {
        List<Order> content = orderRepository.findByFilters(clientId, status, page, size);
        long totalElements = orderRepository.countByFilters(clientId, status);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new OrderPage(content, page, size, totalElements, totalPages);
    }

    @Transactional(readOnly = true)
    public List<OrderHistory> findHistoryByOrderId(UUID orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return orderHistoryRepository.findByOrderId(orderId);
    }
}
