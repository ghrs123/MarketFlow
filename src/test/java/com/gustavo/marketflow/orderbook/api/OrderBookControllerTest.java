package com.gustavo.marketflow.orderbook.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.orderbook.application.OrderBookApplicationService;
import com.gustavo.marketflow.orderbook.application.OrderBookSnapshot;
import com.gustavo.marketflow.orderbook.domain.OrderTask;
import com.gustavo.marketflow.shared.exception.EmptyOrderBookSideException;
import com.gustavo.marketflow.shared.exception.GlobalExceptionHandler;
import com.gustavo.marketflow.shared.exception.OrderAlreadyInBookException;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import com.gustavo.marketflow.shared.exception.OrderNotInCacheException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderBookControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private OrderBookApplicationService orderBookApplicationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OrderBookController controller = new OrderBookController(orderBookApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void postOrdersIdBook_returns202Accepted() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(post("/orders/{id}/book", orderId))
                .andExpect(status().isAccepted());
    }

    @Test
    void postOrdersIdBook_orderNotFound_returns404ProblemDetail() throws Exception {
        UUID orderId = UUID.randomUUID();
        doThrow(new OrderNotFoundException(orderId))
                .when(orderBookApplicationService)
                .addToBook(orderId);

        mockMvc.perform(post("/orders/{id}/book", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-not-found")))
                .andExpect(jsonPath("$.orderId", is(orderId.toString())));
    }

    @Test
    void postOrdersIdBook_duplicate_returns409ProblemDetail() throws Exception {
        UUID orderId = UUID.randomUUID();
        doThrow(new OrderAlreadyInBookException(orderId))
                .when(orderBookApplicationService)
                .addToBook(orderId);

        mockMvc.perform(post("/orders/{id}/book", orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-already-in-book")))
                .andExpect(jsonPath("$.orderId", is(orderId.toString())));
    }

    @Test
    void postOrdersIdBook_invalidUuid_returns400() throws Exception {
        mockMvc.perform(post("/orders/{id}/book", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/invalid-argument")));
    }

    @Test
    void getOrderBook_returns200WithSnapshot() throws Exception {
        OrderTask buyTask = OrderTask.from(OrderTestData.valid());
        OrderTask sellTask = new OrderTask(
                UUID.randomUUID(),
                "C002",
                "MSFT",
                OrderSide.SELL,
                buyTask.quantity(),
                buyTask.price(),
                buyTask.status(),
                buyTask.createdAt()
        );
        when(orderBookApplicationService.getSnapshot()).thenReturn(new OrderBookSnapshot(
                List.of(buyTask),
                List.of(sellTask),
                1,
                1,
                Instant.parse("2026-01-15T10:30:00Z")
        ));

        mockMvc.perform(get("/order-book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyOrders", hasSize(1)))
                .andExpect(jsonPath("$.sellOrders", hasSize(1)))
                .andExpect(jsonPath("$.buyCount", is(1)))
                .andExpect(jsonPath("$.sellCount", is(1)));
    }

    @Test
    void getOrderBookBestBuy_returns200() throws Exception {
        OrderTask buyTask = OrderTask.from(OrderTestData.valid());
        when(orderBookApplicationService.getBestBuy()).thenReturn(buyTask);

        mockMvc.perform(get("/order-book/best-buy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(buyTask.orderId().toString())))
                .andExpect(jsonPath("$.price", is(150.25000000)));
    }

    @Test
    void getOrderBookBestBuy_empty_returns404ProblemDetail() throws Exception {
        when(orderBookApplicationService.getBestBuy()).thenThrow(new EmptyOrderBookSideException("BUY"));

        mockMvc.perform(get("/order-book/best-buy"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-book-side-empty")));
    }

    @Test
    void getOrderBookBestSell_returns200() throws Exception {
        OrderTask orderTask = OrderTask.from(OrderTestData.valid());
        OrderTask sellTask = new OrderTask(
                orderTask.orderId(),
                orderTask.clientId(),
                orderTask.symbol(),
                OrderSide.SELL,
                orderTask.quantity(),
                orderTask.price(),
                orderTask.status(),
                orderTask.createdAt()
        );
        when(orderBookApplicationService.getBestSell()).thenReturn(sellTask);

        mockMvc.perform(get("/order-book/best-sell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(sellTask.orderId().toString())))
                .andExpect(jsonPath("$.price", is(150.25000000)));
    }

    @Test
    void getOrderBookBestSell_empty_returns404ProblemDetail() throws Exception {
        when(orderBookApplicationService.getBestSell()).thenThrow(new EmptyOrderBookSideException("SELL"));

        mockMvc.perform(get("/order-book/best-sell"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-book-side-empty")));
    }

    @Test
    void getOrderBookRecentId_returns200() throws Exception {
        OrderTask orderTask = OrderTask.from(OrderTestData.valid());
        when(orderBookApplicationService.getRecent(orderTask.orderId())).thenReturn(orderTask);

        mockMvc.perform(get("/order-book/recent/{id}", orderTask.orderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(orderTask.orderId().toString())))
                .andExpect(jsonPath("$.price", is(150.25000000)));
    }

    @Test
    void getOrderBookRecentId_notFound_returns404ProblemDetail() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderBookApplicationService.getRecent(orderId)).thenThrow(new OrderNotInCacheException(orderId));

        mockMvc.perform(get("/order-book/recent/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-not-in-cache")))
                .andExpect(jsonPath("$.orderId", is(orderId.toString())));
    }
}
