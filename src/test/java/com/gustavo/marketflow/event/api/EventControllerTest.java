package com.gustavo.marketflow.event.api;

import com.gustavo.marketflow.event.domain.OrderCreatedEvent;
import com.gustavo.marketflow.event.infrastructure.InMemoryEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest {

    private InMemoryEventBus eventBus;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        eventBus = new InMemoryEventBus();
        mockMvc = MockMvcBuilders.standaloneSetup(new EventController(eventBus))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void getEvents_withPublishedEvent_returnsEventHistory() throws Exception {
        UUID orderId = UUID.randomUUID();
        eventBus.publish(OrderCreatedEvent.now(orderId));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("ORDER_CREATED")))
                .andExpect(jsonPath("$[0].orderId", is(orderId.toString())));
    }

    @Test
    void getEventsByType_unknownType_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/events/{type}", "ORDER_EXECUTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
