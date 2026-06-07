package com.gustavo.marketflow.order.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderHistoryJpaRepository;
import com.gustavo.marketflow.order.infrastructure.jpa.SpringDataOrderJpaRepository;
import com.gustavo.marketflow.support.PostgreSqlContainerBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest extends PostgreSqlContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataOrderJpaRepository springDataOrderJpaRepository;

    @Autowired
    private SpringDataOrderHistoryJpaRepository springDataOrderHistoryJpaRepository;

    @BeforeEach
    void setUp() {
        springDataOrderHistoryJpaRepository.deleteAll();
        springDataOrderJpaRepository.deleteAll();
    }

    @Test
    void postOrders_createsOrderAndAddsHistoryEntry() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "clientId", "C001",
                "symbol", "AAPL",
                "side", "BUY",
                "quantity", "100",
                "price", "150.25"
        ));

        String response = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        String id = json.get("id").asText();

        mockMvc.perform(get("/orders/{id}/history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventType", is("ORDER_CREATED")));
    }

    @Test
    void getOrders_filtersByClientIdAndStatusWithPagination() throws Exception {
        createOrder("C001", "AAPL");
        createOrder("C001", "MSFT");
        createOrder("C002", "GOOG");

        mockMvc.perform(get("/orders")
                        .queryParam("clientId", "C001")
                        .queryParam("status", "NEW")
                        .queryParam("page", "0")
                        .queryParam("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)));
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
                .andExpect(status().isBadRequest());
    }

    private void createOrder(String clientId, String symbol) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "clientId", clientId,
                "symbol", symbol,
                "side", "BUY",
                "quantity", "10",
                "price", "101.50"
        ));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
