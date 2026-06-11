package com.gustavo.marketflow.resilience.api;

import com.gustavo.marketflow.resilience.application.MarketDataQuote;
import com.gustavo.marketflow.resilience.infrastructure.MarketDataClient;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for simulated external-service calls used in resilience exercises.
 */
@RestController
@RequestMapping("/external")
@Validated
@PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class ExternalServiceController {

    private final MarketDataClient marketDataClient;

    public ExternalServiceController(MarketDataClient marketDataClient) {
        this.marketDataClient = marketDataClient;
    }

    /**
     * Retrieves simulated market data while preserving the client-facing fallback contract.
     */
    @GetMapping("/market-data/{symbol}")
    public MarketDataQuote marketData(
            @PathVariable
            @Pattern(regexp = "[A-Za-z_]{1,20}", message = "symbol contains unsupported characters")
            String symbol
    ) {
        return marketDataClient.findQuote(symbol);
    }
}
