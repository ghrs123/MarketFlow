package com.gustavo.marketflow.order.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustavo.marketflow.shared.exception.GlobalExceptionHandler;
import com.gustavo.marketflow.order.application.OrderApplicationService;
import com.gustavo.marketflow.order.infrastructure.OrderInMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice-style test of the REST adapter using a standalone MockMvc setup.
 *
 * <p>Wires the real controller, real service and real in-memory repository
 * together with the {@link GlobalExceptionHandler} so the full
 * request-validation-error path is exercised, without paying the cost of
 * a full Spring context.</p>
 */
class OrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        OrderApplicationService service = new OrderApplicationService(new OrderInMemoryRepository());
        OrderController controller = new OrderController(service);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void postOrders_createsOrderAndReturns201WithLocation() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "clientId", "C001",
                "symbol", "AAPL",
                "side", "BUY",
                "quantity", "100",
                "price", "150.25"
        ));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.clientId", is("C001")))
                .andExpect(jsonPath("$.status", is("NEW")));
    }

    @Test
    void postOrders_returns400WhenValidationFails() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "clientId", "",
                "symbol", "AAPL",
                "side", "BUY",
                "quantity", "-1",
                "price", "0"
        ));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation error")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors", notNullValue()));
    }

    @Test
    void postOrders_returns400WhenBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad request")));
    }

    @Test
    void getOrders_returns404WhenOrderDoesNotExist() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/orders/{id}", randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Order not found")))
                .andExpect(jsonPath("$.orderId", is(randomId.toString())));
    }

    @Test
    void getOrders_returnsCreatedOrder() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "clientId", "C001",
                "symbol", "AAPL",
                "side", "BUY",
                "quantity", "10",
                "price", "1.50"
        ));

        String response = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.clientId", is("C001")));
    }

    @Test
    void getOrders_listsAllOrders() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "clientId", "C001",
                "symbol", "AAPL",
                "side", "BUY",
                "quantity", "1",
                "price", "1"
        ));

        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)));
    }
}
