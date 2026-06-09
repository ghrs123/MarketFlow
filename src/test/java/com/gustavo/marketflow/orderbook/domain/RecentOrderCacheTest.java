package com.gustavo.marketflow.orderbook.domain;

import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RecentOrderCacheTest {

    @Test
    void putAndGet_returnsCachedItem() {
        RecentOrderCache cache = new RecentOrderCache(2);
        OrderTask cached = orderTask("cached", "2026-01-15T10:30:00Z");

        cache.put(cached);

        assertThat(cache.get(cached.orderId())).contains(cached);
        assertThat(cache.get(cached.orderId()).orElseThrow().price()).isEqualByComparingTo("150.25");
    }

    @Test
    void put_capacityExceeded_evictsLeastRecentlyUsedItem() {
        RecentOrderCache cache = new RecentOrderCache(2);
        OrderTask first = orderTask("first", "2026-01-15T10:30:00Z");
        OrderTask second = orderTask("second", "2026-01-15T10:31:00Z");
        OrderTask third = orderTask("third", "2026-01-15T10:32:00Z");

        cache.put(first);
        cache.put(second);
        cache.put(third);

        assertThat(cache.get(first.orderId())).isEmpty();
        assertThat(cache.get(second.orderId())).contains(second);
        assertThat(cache.get(third.orderId())).contains(third);
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void get_accessUpdatesRecency_recentlyAccessedItemIsNotEvicted() {
        RecentOrderCache cache = new RecentOrderCache(2);
        OrderTask first = orderTask("first", "2026-01-15T10:30:00Z");
        OrderTask second = orderTask("second", "2026-01-15T10:31:00Z");
        OrderTask third = orderTask("third", "2026-01-15T10:32:00Z");

        cache.put(first);
        cache.put(second);
        cache.get(first.orderId());
        cache.put(third);

        assertThat(cache.get(first.orderId())).contains(first);
        assertThat(cache.get(second.orderId())).isEmpty();
        assertThat(cache.get(third.orderId())).contains(third);
    }

    private OrderTask orderTask(String suffix, String createdAt) {
        return new OrderTask(
                UUID.randomUUID(),
                "C001",
                "AAPL-" + suffix,
                OrderSide.BUY,
                new BigDecimal("100.00"),
                new BigDecimal("150.25"),
                OrderStatus.NEW,
                Instant.parse(createdAt)
        );
    }
}
