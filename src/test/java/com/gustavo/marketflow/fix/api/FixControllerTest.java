package com.gustavo.marketflow.fix.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import com.gustavo.marketflow.fix.application.FixMessageApplicationService;
import com.gustavo.marketflow.fix.domain.FixMessage;
import com.gustavo.marketflow.fix.domain.FixTagExplanation;
import com.gustavo.marketflow.shared.exception.FixMessageAlreadyExistsException;
import com.gustavo.marketflow.shared.exception.FixMessageNotFoundException;
import com.gustavo.marketflow.shared.exception.GlobalExceptionHandler;
import com.gustavo.marketflow.shared.exception.InvalidFixMessageException;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FixControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private FixMessageApplicationService fixMessageApplicationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new FixController(fixMessageApplicationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void postOrderFixMessage_returns201AndLocation() throws Exception {
        UUID orderId = UUID.randomUUID();
        FixMessage fixMessage = fixMessage(orderId);
        when(fixMessageApplicationService.generateForOrder(orderId)).thenReturn(fixMessage);

        mockMvc.perform(post("/orders/{id}/fix-message", orderId))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/orders/" + orderId + "/fix-message")))
                .andExpect(jsonPath("$.orderId", is(orderId.toString())))
                .andExpect(jsonPath("$.rawMessage", is(fixMessage.rawMessage())));
    }

    @Test
    void postOrderFixMessage_existingMessage_returns409() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(fixMessageApplicationService.generateForOrder(orderId))
                .thenThrow(new FixMessageAlreadyExistsException(orderId));

        mockMvc.perform(post("/orders/{id}/fix-message", orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type", is(
                        "https://marketflow.local/errors/fix-message-already-exists")));
    }

    @Test
    void getOrderFixMessage_existingMessage_returns200() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(fixMessageApplicationService.findByOrderId(orderId)).thenReturn(fixMessage(orderId));

        mockMvc.perform(get("/orders/{id}/fix-message", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(orderId.toString())));
    }

    @Test
    void getOrderFixMessage_missingMessage_returns404() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(fixMessageApplicationService.findByOrderId(orderId))
                .thenThrow(new FixMessageNotFoundException(orderId));

        mockMvc.perform(get("/orders/{id}/fix-message", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type", is(
                        "https://marketflow.local/errors/fix-message-not-found")));
    }

    @Test
    void getOrderFixExplanation_returnsExplainedTags() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(fixMessageApplicationService.explainByOrderId(orderId)).thenReturn(explanations());

        mockMvc.perform(get("/orders/{id}/fix-explanation", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(orderId.toString())))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags[0].name", is("BeginString")));
    }

    @Test
    void postFixExplain_validRawMessage_returnsExplainedTags() throws Exception {
        String rawMessage = "8=FIX.4.4|35=D";
        when(fixMessageApplicationService.explainRaw(rawMessage)).thenReturn(explanations());

        mockMvc.perform(post("/fix/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RawFixMessageRequest(rawMessage))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags[1].name", is("MsgType")));
    }

    @Test
    void postFixExplain_blankRawMessage_returns400ValidationProblem() throws Exception {
        mockMvc.perform(post("/fix/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RawFixMessageRequest(" "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is("https://marketflow.local/errors/validation")));
    }

    @Test
    void postFixExplain_invalidRawMessage_returns400FixProblem() throws Exception {
        String rawMessage = "invalid";
        when(fixMessageApplicationService.explainRaw(rawMessage))
                .thenThrow(new InvalidFixMessageException("Invalid FIX field"));

        mockMvc.perform(post("/fix/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RawFixMessageRequest(rawMessage))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type", is(
                        "https://marketflow.local/errors/invalid-fix-message")));
    }

    private FixMessage fixMessage(UUID orderId) {
        Instant now = Instant.parse("2026-01-15T10:30:00Z");
        return new FixMessage(
                UUID.randomUUID(),
                orderId,
                "8=FIX.4.4|35=D|55=AAPL",
                now,
                now
        );
    }

    private List<FixTagExplanation> explanations() {
        return List.of(
                new FixTagExplanation("8", "BeginString", "FIX.4.4", "Protocol version", true),
                new FixTagExplanation("35", "MsgType", "D", "Message type", true)
        );
    }
}
