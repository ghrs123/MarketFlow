package com.gustavo.marketflow.order.application;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Coordinates idempotency lookups against the durable unique order key.
 */
@Component
public class IdempotencyRegistry {

    private final OrderRepository orderRepository;

    public IdempotencyRegistry(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Optional<Order> findExisting(String idempotencyKey) {
        return orderRepository.findByIdempotencyKey(idempotencyKey);
    }
}
