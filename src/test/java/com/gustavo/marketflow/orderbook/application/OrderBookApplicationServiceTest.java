package com.gustavo.marketflow.orderbook.application;

import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.application.OrderApplicationService;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.orderbook.domain.OrderBook;
import com.gustavo.marketflow.orderbook.domain.OrderTask;
import com.gustavo.marketflow.orderbook.domain.RecentOrderCache;
import com.gustavo.marketflow.shared.exception.EmptyOrderBookSideException;
import com.gustavo.marketflow.shared.exception.OrderAlreadyInBookException;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import com.gustavo.marketflow.shared.exception.OrderNotInCacheException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderBookApplicationServiceTest {

    @Mock
    private OrderApplicationService orderApplicationService;

    @Mock
    private OrderBook orderBook;

    @Mock
    private RecentOrderCache recentOrderCache;

    @InjectMocks
    private OrderBookApplicationService service;

    @Test
    void addToBook_happyPath_loadsOrderAddsToBookAndCache() {
        Order order = OrderTestData.valid();
        OrderTask expectedTask = OrderTask.from(order);
        when(orderApplicationService.findById(order.getId())).thenReturn(order);
        when(orderBook.add(expectedTask)).thenReturn(true);

        service.addToBook(order.getId());

        verify(orderApplicationService).findById(order.getId());
        verify(orderBook).add(expectedTask);
        verify(recentOrderCache).put(expectedTask);
    }

    @Test
    void addToBook_orderNotFound_propagatesOrderNotFoundException() {
        UUID orderId = UUID.randomUUID();
        when(orderApplicationService.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));

        assertThatThrownBy(() -> service.addToBook(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(orderId.toString());
    }

    @Test
    void addToBook_duplicate_throwsOrderAlreadyInBookException() {
        Order order = OrderTestData.valid();
        OrderTask orderTask = OrderTask.from(order);
        when(orderApplicationService.findById(order.getId())).thenReturn(order);
        when(orderBook.add(orderTask)).thenReturn(false);

        assertThatThrownBy(() -> service.addToBook(order.getId()))
                .isInstanceOf(OrderAlreadyInBookException.class)
                .hasMessageContaining(order.getId().toString());
    }

    @Test
    void getSnapshot_returnsListsCountsAndTimestamp() {
        Order buyOrder = OrderTestData.valid();
        Order sellOrder = OrderTestData.withSymbol("MSFT");
        OrderTask buyTask = OrderTask.from(buyOrder);
        OrderTask sellTask = new OrderTask(
                sellOrder.getId(),
                sellOrder.getClientId(),
                sellOrder.getSymbol(),
                com.gustavo.marketflow.order.domain.OrderSide.SELL,
                sellOrder.getQuantity(),
                sellOrder.getPrice(),
                sellOrder.getStatus(),
                sellOrder.getCreatedAt()
        );

        when(orderBook.buySnapshot()).thenReturn(List.of(buyTask));
        when(orderBook.sellSnapshot()).thenReturn(List.of(sellTask));

        OrderBookSnapshot snapshot = service.getSnapshot();

        assertThat(snapshot.buyOrders()).containsExactly(buyTask);
        assertThat(snapshot.sellOrders()).containsExactly(sellTask);
        assertThat(snapshot.buyCount()).isEqualTo(1);
        assertThat(snapshot.sellCount()).isEqualTo(1);
        assertThat(snapshot.timestamp()).isNotNull();
    }

    @Test
    void getBestBuy_happyPath_returnsOrderTask() {
        OrderTask buyTask = OrderTask.from(OrderTestData.valid());
        when(orderBook.bestBuy()).thenReturn(Optional.of(buyTask));

        OrderTask result = service.getBestBuy();

        assertThat(result).isEqualTo(buyTask);
        assertThat(result.price()).isEqualByComparingTo("150.25000000");
    }

    @Test
    void getBestBuy_empty_throwsEmptyOrderBookSideException() {
        when(orderBook.bestBuy()).thenReturn(Optional.empty());

        assertThatThrownBy(service::getBestBuy)
                .isInstanceOf(EmptyOrderBookSideException.class)
                .hasMessageContaining("BUY");
    }

    @Test
    void getBestSell_happyPath_returnsOrderTask() {
        Order order = OrderTestData.valid();
        OrderTask sellTask = new OrderTask(
                order.getId(),
                order.getClientId(),
                order.getSymbol(),
                com.gustavo.marketflow.order.domain.OrderSide.SELL,
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
        when(orderBook.bestSell()).thenReturn(Optional.of(sellTask));

        OrderTask result = service.getBestSell();

        assertThat(result).isEqualTo(sellTask);
        assertThat(result.price()).isEqualByComparingTo("150.25000000");
    }

    @Test
    void getBestSell_empty_throwsEmptyOrderBookSideException() {
        when(orderBook.bestSell()).thenReturn(Optional.empty());

        assertThatThrownBy(service::getBestSell)
                .isInstanceOf(EmptyOrderBookSideException.class)
                .hasMessageContaining("SELL");
    }

    @Test
    void getRecent_hit_returnsOrderTaskFromCache() {
        OrderTask orderTask = OrderTask.from(OrderTestData.valid());
        when(recentOrderCache.get(orderTask.orderId())).thenReturn(Optional.of(orderTask));

        OrderTask result = service.getRecent(orderTask.orderId());

        assertThat(result).isEqualTo(orderTask);
        assertThat(result.price()).isEqualByComparingTo("150.25000000");
    }

    @Test
    void getRecent_miss_throwsOrderNotInCacheException() {
        UUID orderId = UUID.randomUUID();
        when(recentOrderCache.get(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRecent(orderId))
                .isInstanceOf(OrderNotInCacheException.class)
                .hasMessageContaining(orderId.toString());
    }
}
