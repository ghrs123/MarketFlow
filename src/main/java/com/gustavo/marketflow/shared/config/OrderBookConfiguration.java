package com.gustavo.marketflow.shared.config;

import com.gustavo.marketflow.orderbook.domain.OrderBook;
import com.gustavo.marketflow.orderbook.domain.RecentOrderCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers singleton in-memory structures used by the Phase 4 order book.
 *
 * <p>The order book and recent-order cache are process-local in this phase and
 * deliberately not persisted, so singleton beans are the simplest lifecycle
 * that matches the intended behaviour.</p>
 */
@Configuration
public class OrderBookConfiguration {

    private static final int RECENT_ORDER_CACHE_CAPACITY = 100;

    @Bean
    public OrderBook orderBook() {
        return new OrderBook();
    }

    @Bean
    public RecentOrderCache recentOrderCache() {
        return new RecentOrderCache(RECENT_ORDER_CACHE_CAPACITY);
    }
}
