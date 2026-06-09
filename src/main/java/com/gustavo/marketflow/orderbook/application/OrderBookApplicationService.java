package com.gustavo.marketflow.orderbook.application;

import com.gustavo.marketflow.order.application.OrderApplicationService;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.orderbook.domain.OrderBook;
import com.gustavo.marketflow.orderbook.domain.OrderTask;
import com.gustavo.marketflow.orderbook.domain.RecentOrderCache;
import com.gustavo.marketflow.shared.exception.EmptyOrderBookSideException;
import com.gustavo.marketflow.shared.exception.OrderAlreadyInBookException;
import com.gustavo.marketflow.shared.exception.OrderNotInCacheException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates the in-memory order-book use cases for this phase.
 *
 * <p>The service bridges persisted orders and the in-memory structures. It
 * loads the source order from the existing order API slice and then updates
 * only in-memory data structures.</p>
 */
@Service
public class OrderBookApplicationService {

    private final OrderApplicationService orderApplicationService;
    private final OrderBook orderBook;
    private final RecentOrderCache recentOrderCache;

    public OrderBookApplicationService(OrderApplicationService orderApplicationService,
                                       OrderBook orderBook,
                                       RecentOrderCache recentOrderCache) {
        this.orderApplicationService = orderApplicationService;
        this.orderBook = orderBook;
        this.recentOrderCache = recentOrderCache;
    }

    public void addToBook(UUID orderId) {
        Order order = orderApplicationService.findById(orderId);
        OrderTask orderTask = OrderTask.from(order);
        boolean added = orderBook.add(orderTask);
        if (!added) {
            throw new OrderAlreadyInBookException(orderId);
        }
        recentOrderCache.put(orderTask);
    }

    public OrderBookSnapshot getSnapshot() {
        List<OrderTask> buyOrders = orderBook.buySnapshot();
        List<OrderTask> sellOrders = orderBook.sellSnapshot();
        return new OrderBookSnapshot(
                buyOrders,
                sellOrders,
                buyOrders.size(),
                sellOrders.size(),
                Instant.now()
        );
    }

    public OrderTask getBestBuy() {
        return orderBook.bestBuy()
                .orElseThrow(() -> new EmptyOrderBookSideException("BUY"));
    }

    public OrderTask getBestSell() {
        return orderBook.bestSell()
                .orElseThrow(() -> new EmptyOrderBookSideException("SELL"));
    }

    public OrderTask getRecent(UUID orderId) {
        return recentOrderCache.get(orderId)
                .orElseThrow(() -> new OrderNotInCacheException(orderId));
    }
}
