package com.gustavo.marketflow.order.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.application.OrderApplicationService;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderHistory;
import com.gustavo.marketflow.order.domain.OrderPage;
import com.gustavo.marketflow.order.domain.OrderStatus;
import com.gustavo.marketflow.shared.exception.GlobalExceptionHandler;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private OrderApplicationService orderApplicationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        OrderController controller = new OrderController(orderApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void postOrders_returns201AndLocationHeader() throws Exception {
        Order createdOrder = OrderTestData.valid();
        when(orderApplicationService.createOrder(any(), any(), any(), any(), any())).thenReturn(createdOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateOrderRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/orders/" + createdOrder.getId())))
                .andExpect(jsonPath("$.id", is(createdOrder.getId().toString())))
                .andExpect(jsonPath("$.clientId", is(createdOrder.getClientId())))
                .andExpect(jsonPath("$.status", is(createdOrder.getStatus().name())));
    }

    @Test
    void postOrders_returns400WhenValidationFails() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "clientId", "",
                                "symbol", "AAPL",
                                "side", "BUY",
                                "quantity", "-1",
                                "price", "0"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/validation")))
                .andExpect(jsonPath("$.title", is("Validation error")))
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    void postOrders_returns400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":"C001","symbol":"AAPL",
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/malformed-request")))
                .andExpect(jsonPath("$.detail", is("Malformed request body")));
    }

    @Test
    void postOrders_returns400WhenEnumIsInvalid() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "clientId", "C001",
                                "symbol", "AAPL",
                                "side", "INVALID",
                                "quantity", "10",
                                "price", "150.25"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/malformed-request")))
                .andExpect(jsonPath("$.detail", is("Malformed request body")));
    }

    @Test
    void getOrderById_existingOrder_returns200() throws Exception {
        Order existingOrder = OrderTestData.valid();
        when(orderApplicationService.findById(existingOrder.getId())).thenReturn(existingOrder);

        mockMvc.perform(get("/orders/{id}", existingOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(existingOrder.getId().toString())))
                .andExpect(jsonPath("$.clientId", is(existingOrder.getClientId())))
                .andExpect(jsonPath("$.symbol", is(existingOrder.getSymbol())))
                .andExpect(jsonPath("$.status", is(existingOrder.getStatus().name())));
    }

    @Test
    void getOrderById_missingOrder_returns404ProblemDetail() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderApplicationService.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-not-found")))
                .andExpect(jsonPath("$.title", is("Order not found")))
                .andExpect(jsonPath("$.orderId", is(orderId.toString())));
    }

    @Test
    void getOrderById_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/orders/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/invalid-argument")));
    }

    @Test
    void getOrders_withFiltersAndPagination_returns200() throws Exception {
        OrderPage page = new OrderPage(
                List.of(OrderTestData.withClientId("C001")),
                0,
                10,
                1,
                1
        );
        when(orderApplicationService.findByFilters("C001", OrderStatus.NEW, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/orders")
                        .queryParam("clientId", "C001")
                        .queryParam("status", "NEW")
                        .queryParam("page", "0")
                        .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].clientId", is("C001")))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getOrderHistory_returns200WithEvents() throws Exception {
        UUID orderId = UUID.randomUUID();
        List<OrderHistory> history = List.of(
                new OrderHistory(
                        UUID.randomUUID(),
                        orderId,
                        "ORDER_CREATED",
                        null,
                        OrderStatus.NEW,
                        Instant.parse("2026-01-15T10:30:00Z"),
                        null,
                        Instant.parse("2026-01-15T10:30:00Z")
                )
        );
        when(orderApplicationService.findHistoryByOrderId(orderId)).thenReturn(history);

        mockMvc.perform(get("/orders/{id}/history", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId", is(orderId.toString())))
                .andExpect(jsonPath("$[0].eventType", is("ORDER_CREATED")))
                .andExpect(jsonPath("$[0].newStatus", is(OrderStatus.NEW.name())));
    }

    private Map<String, Object> validCreateOrderRequest() {
        return Map.of(
                "clientId", "C001",
                "symbol", "AAPL",
                "side", "BUY",
                "quantity", "10",
                "price", "150.25"
        );
    }
}
