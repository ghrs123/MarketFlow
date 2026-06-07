package com.gustavo.marketflow.order.infrastructure;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory adapter for {@link OrderRepository}.
 *
 * <p>Backed by a {@link ConcurrentHashMap} so that concurrent HTTP requests
 * served by the Tomcat thread pool can safely read and write without
 * external synchronization. {@code put}/{@code get} are atomic on the
 * underlying buckets, and iteration via {@code values()} is weakly
 * consistent (does not throw {@link java.util.ConcurrentModificationException}).</p>
 *
 * <p>Phase 1 limitations explicitly accepted as trade-offs:
 * <ul>
 *   <li>State is lost on restart (no persistence).</li>
 *   <li>{@link #findAll()} returns an unbounded snapshot - acceptable for a
 *       study lab, replaced by paginated JPA queries in Phase 2.</li>
 * </ul>
 */
@Repository
public class OrderInMemoryRepository implements OrderRepository {

    private final ConcurrentHashMap<UUID, Order> store = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        store.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAll() {
        // Defensive copy: callers receive an immutable snapshot and cannot
        // mutate the internal store.
        return List.copyOf(store.values());
    }
}
