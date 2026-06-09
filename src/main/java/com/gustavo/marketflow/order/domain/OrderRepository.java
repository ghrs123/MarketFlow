package com.gustavo.marketflow.order.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for order persistence.
 *
 * <p>Defined in the domain layer (Hexagonal/Ports-and-Adapters style) so
 * that the application service depends on an abstraction, not on the
 * in-memory adapter. This is what allows Phase 2 to swap the in-memory
 * implementation for a JPA-based one without touching the service.</p>
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    List<Order> findAll();

    List<Order> findByFilters(String clientId, OrderStatus status, int page, int size);

    long countByFilters(String clientId, OrderStatus status);

    Order updateStatus(UUID id, OrderStatus status, Instant updatedAt);
}
