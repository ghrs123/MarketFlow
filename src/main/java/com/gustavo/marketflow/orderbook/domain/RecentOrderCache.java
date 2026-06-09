package com.gustavo.marketflow.orderbook.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores recently added order-book items using LRU eviction.
 *
 * <p>The cache is intentionally in-memory and bounded for this phase so the
 * lab can demonstrate recency semantics and the memory/performance trade-off
 * without introducing persistence or external infrastructure.</p>
 */
public class RecentOrderCache {

    private final Map<UUID, OrderTask> cache;

    public RecentOrderCache(int capacity) {
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, OrderTask> eldest) {
                return size() > capacity;
            }
        };
    }

    public void put(OrderTask orderTask) {
        synchronized (cache) {
            cache.put(orderTask.orderId(), orderTask);
        }
    }

    public Optional<OrderTask> get(UUID orderId) {
        synchronized (cache) {
            return Optional.ofNullable(cache.get(orderId));
        }
    }

    public int size() {
        synchronized (cache) {
            return cache.size();
        }
    }
}
