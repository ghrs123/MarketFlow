package com.gustavo.marketflow.execution.api;

import com.gustavo.marketflow.execution.application.ExecutionStats;
import com.gustavo.marketflow.execution.application.OrderProcessingEngine;
import com.gustavo.marketflow.execution.application.OrderExecutionService;
import com.gustavo.marketflow.execution.domain.DeadLetterMessage;
import com.gustavo.marketflow.resilience.application.BrokerExecutionResult;
import com.gustavo.marketflow.shared.exception.GlobalExceptionHandler;
import com.gustavo.marketflow.shared.exception.OrderAlreadyQueuedException;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import com.gustavo.marketflow.shared.exception.OrderNotQueueableException;
import com.gustavo.marketflow.shared.exception.OrderQueueFullException;
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
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExecutionControllerTest {

    @Mock
    private OrderProcessingEngine orderProcessingEngine;

    @Mock
    private OrderExecutionService orderExecutionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ExecutionController controller = new ExecutionController(orderProcessingEngine, orderExecutionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void postOrdersIdQueue_returns202Accepted() throws Exception {
        mockMvc.perform(post("/orders/{id}/queue", UUID.randomUUID()))
                .andExpect(status().isAccepted());
    }

    @Test
    void postOrdersIdQueue_orderNotFound_returns404() throws Exception {
        UUID orderId = UUID.randomUUID();
        doThrow(new OrderNotFoundException(orderId)).when(orderProcessingEngine).enqueue(orderId);

        mockMvc.perform(post("/orders/{id}/queue", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-not-found")));
    }

    @Test
    void postOrdersIdQueue_duplicate_returns409() throws Exception {
        UUID orderId = UUID.randomUUID();
        doThrow(new OrderAlreadyQueuedException(orderId)).when(orderProcessingEngine).enqueue(orderId);

        mockMvc.perform(post("/orders/{id}/queue", orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-already-queued")));
    }

    @Test
    void postOrdersIdQueue_notQueueable_returns409() throws Exception {
        UUID orderId = UUID.randomUUID();
        doThrow(new OrderNotQueueableException(orderId, com.gustavo.marketflow.order.domain.OrderStatus.EXECUTED))
                .when(orderProcessingEngine).enqueue(orderId);

        mockMvc.perform(post("/orders/{id}/queue", orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-not-queueable")));
    }

    @Test
    void postOrdersIdQueue_queueFull_returns503() throws Exception {
        UUID orderId = UUID.randomUUID();
        doThrow(new OrderQueueFullException(orderId)).when(orderProcessingEngine).enqueue(orderId);

        mockMvc.perform(post("/orders/{id}/queue", orderId))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/order-queue-full")));
    }

    @Test
    void postOrdersIdQueue_invalidUuid_returns400() throws Exception {
        mockMvc.perform(post("/orders/{id}/queue", "invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/invalid-argument")));
    }

    @Test
    void postExecuteWithBroker_success_returnsBrokerOutcome() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderExecutionService.executeWithBroker(orderId)).thenReturn(new BrokerExecutionResult(
                orderId,
                "BROKER_ACCEPTED",
                "BRK-123",
                false,
                Instant.parse("2026-01-15T10:30:00Z")
        ));

        mockMvc.perform(post("/orders/{id}/execute-with-broker", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(orderId.toString())))
                .andExpect(jsonPath("$.status", is("BROKER_ACCEPTED")))
                .andExpect(jsonPath("$.fallback", is(false)));
    }

    @Test
    void postExecutionStart_returns202Accepted() throws Exception {
        mockMvc.perform(post("/execution/start"))
                .andExpect(status().isAccepted());
    }

    @Test
    void postExecutionStop_returns202Accepted() throws Exception {
        mockMvc.perform(post("/execution/stop"))
                .andExpect(status().isAccepted());
    }

    @Test
    void getExecutionStats_returns200() throws Exception {
        when(orderProcessingEngine.getStats()).thenReturn(new ExecutionStats(
                true,
                4,
                4,
                2,
                10,
                8,
                6,
                2,
                Instant.parse("2026-01-15T10:30:00Z")
        ));

        mockMvc.perform(get("/execution/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running", is(true)))
                .andExpect(jsonPath("$.configuredWorkers", is(4)))
                .andExpect(jsonPath("$.activeWorkers", is(4)))
                .andExpect(jsonPath("$.queueSize", is(2)))
                .andExpect(jsonPath("$.totalQueued", is(10)))
                .andExpect(jsonPath("$.totalProcessed", is(8)))
                .andExpect(jsonPath("$.totalSucceeded", is(6)))
                .andExpect(jsonPath("$.totalFailed", is(2)));
    }

    @Test
    void getExecutionDlq_withMessage_returnsDeadLetterEntries() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderProcessingEngine.getDeadLetters()).thenReturn(List.of(new DeadLetterMessage(
                UUID.randomUUID(),
                orderId,
                "Simulated processing failure",
                3,
                Map.of("correlationId", "test"),
                Instant.parse("2026-01-15T10:30:00Z")
        )));

        mockMvc.perform(get("/execution/dlq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId", is(orderId.toString())))
                .andExpect(jsonPath("$[0].attempts", is(3)));
    }

    @Test
    void postExecutionDlqReprocess_existingMessage_returns202() throws Exception {
        UUID orderId = UUID.randomUUID();

        mockMvc.perform(post("/execution/dlq/{orderId}/reprocess", orderId))
                .andExpect(status().isAccepted());
    }
}
