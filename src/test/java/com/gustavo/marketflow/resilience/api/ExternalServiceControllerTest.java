package com.gustavo.marketflow.resilience.api;

import com.gustavo.marketflow.resilience.application.MarketDataQuote;
import com.gustavo.marketflow.resilience.infrastructure.MarketDataClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExternalServiceControllerTest {

    @Mock
    private MarketDataClient marketDataClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExternalServiceController(marketDataClient))
                .setValidator(validator)
                .build();
    }

    @Test
    void marketData_validSymbol_returnsQuote() throws Exception {
        when(marketDataClient.findQuote("AAPL")).thenReturn(new MarketDataQuote(
                "AAPL",
                new BigDecimal("100.00000000"),
                "SIMULATED_LIVE",
                false,
                Instant.parse("2026-01-15T10:30:00Z")
        ));

        mockMvc.perform(get("/external/market-data/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.source").value("SIMULATED_LIVE"))
                .andExpect(jsonPath("$.fallback").value(false));
    }

}
