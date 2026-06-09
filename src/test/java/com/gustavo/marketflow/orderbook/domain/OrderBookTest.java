package com.gustavo.marketflow.orderbook.domain;

import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBookTest {

    @Test
    void add_buyOrders_ordersHighestPriceFirst() {
        OrderBook orderBook = new OrderBook();
        OrderTask lower = orderTask("buy-low", OrderSide.BUY, "10.00", "2026-01-15T10:30:00Z");
        OrderTask higher = orderTask("buy-high", OrderSide.BUY, "15.00", "2026-01-15T10:31:00Z");

        orderBook.add(lower);
        orderBook.add(higher);

        assertThat(orderBook.bestBuy()).contains(higher);
        assertThat(orderBook.bestBuy().orElseThrow().price()).isEqualByComparingTo("15.00");
    }

    @Test
    void add_sellOrders_ordersLowestPriceFirst() {
        OrderBook orderBook = new OrderBook();
        OrderTask higher = orderTask("sell-high", OrderSide.SELL, "20.00", "2026-01-15T10:30:00Z");
        OrderTask lower = orderTask("sell-low", OrderSide.SELL, "18.50", "2026-01-15T10:31:00Z");

        orderBook.add(higher);
        orderBook.add(lower);

        assertThat(orderBook.bestSell()).contains(lower);
        assertThat(orderBook.bestSell().orElseThrow().price()).isEqualByComparingTo("18.50");
    }

    @Test
    void add_samePrice_keepsOldestCreatedAtFirst() {
        OrderBook orderBook = new OrderBook();
        OrderTask older = orderTask("older", OrderSide.BUY, "12.00", "2026-01-15T10:30:00Z");
        OrderTask newer = orderTask("newer", OrderSide.BUY, "12.00", "2026-01-15T10:31:00Z");

        orderBook.add(newer);
        orderBook.add(older);

        assertThat(orderBook.bestBuy()).contains(older);
        assertThat(orderBook.buySnapshot()).containsExactly(older, newer);
    }

    @Test
    void add_duplicateOrderId_returnsFalse() {
        OrderBook orderBook = new OrderBook();
        UUID orderId = UUID.randomUUID();
        OrderTask first = orderTask(orderId, "first", OrderSide.BUY, "10.00", "2026-01-15T10:30:00Z");
        OrderTask duplicate = orderTask(orderId, "duplicate", OrderSide.SELL, "9.00", "2026-01-15T10:31:00Z");

        boolean firstAdded = orderBook.add(first);
        boolean duplicateAdded = orderBook.add(duplicate);

        assertThat(firstAdded).isTrue();
        assertThat(duplicateAdded).isFalse();
        assertThat(orderBook.size()).isEqualTo(1);
    }

    @Test
    void bestBuy_emptyBook_returnsEmpty() {
        OrderBook orderBook = new OrderBook();

        assertThat(orderBook.bestBuy()).isEmpty();
    }

    @Test
    void bestSell_emptyBook_returnsEmpty() {
        OrderBook orderBook = new OrderBook();

        assertThat(orderBook.bestSell()).isEmpty();
    }

    @Test
    void snapshot_returnsCurrentBuyAndSellStateInCorrectOrder() {
        OrderBook orderBook = new OrderBook();
        OrderTask buyFirst = orderTask("buy-first", OrderSide.BUY, "14.00", "2026-01-15T10:31:00Z");
        OrderTask buySecond = orderTask("buy-second", OrderSide.BUY, "11.00", "2026-01-15T10:32:00Z");
        OrderTask sellFirst = orderTask("sell-first", OrderSide.SELL, "20.00", "2026-01-15T10:30:00Z");
        OrderTask sellSecond = orderTask("sell-second", OrderSide.SELL, "21.50", "2026-01-15T10:33:00Z");

        orderBook.add(sellSecond);
        orderBook.add(buySecond);
        orderBook.add(sellFirst);
        orderBook.add(buyFirst);

        List<OrderTask> buySnapshot = orderBook.buySnapshot();
        List<OrderTask> sellSnapshot = orderBook.sellSnapshot();

        assertThat(buySnapshot).containsExactly(buyFirst, buySecond);
        assertThat(sellSnapshot).containsExactly(sellFirst, sellSecond);
        assertThat(buySnapshot.getFirst().price()).isEqualByComparingTo("14.00");
        assertThat(sellSnapshot.getFirst().price()).isEqualByComparingTo("20.00");
    }

    private OrderTask orderTask(String suffix, OrderSide side, String price, String createdAt) {
        return orderTask(UUID.randomUUID(), suffix, side, price, createdAt);
    }

    private OrderTask orderTask(UUID orderId, String suffix, OrderSide side, String price, String createdAt) {
        return new OrderTask(
                orderId,
                "C001",
                "AAPL-" + suffix,
                side,
                new BigDecimal("100.00"),
                new BigDecimal(price),
                OrderStatus.NEW,
                Instant.parse(createdAt)
        );
    }
}
