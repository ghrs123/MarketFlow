package com.gustavo.marketflow.resilience.infrastructure;

import com.gustavo.marketflow.resilience.application.MarketDataQuote;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

/**
 * Simulates market-data retrieval and provides a stale quote as graceful degradation.
 */
@Component
public class MarketDataClient {

    private static final Logger log = LoggerFactory.getLogger(MarketDataClient.class);
    private static final String FAILURE_SYMBOL = "MARKET_FAIL";

    /**
     * Returns a live quote or a stale fallback when the dependency is unavailable.
     */
    @CircuitBreaker(name = "marketData", fallbackMethod = "fallbackQuote")
    public MarketDataQuote findQuote(String symbol) {
        if (FAILURE_SYMBOL.equalsIgnoreCase(symbol)) {
            throw new TransientExternalServiceException("Simulated market-data failure");
        }
        return new MarketDataQuote(
                symbol.toUpperCase(Locale.ROOT),
                new BigDecimal("100.00000000"),
                "SIMULATED_LIVE",
                false,
                Instant.now()
        );
    }

    private MarketDataQuote fallbackQuote(String symbol,
                                          TransientExternalServiceException cause) {
        return staleQuote(symbol, cause);
    }

    private MarketDataQuote fallbackQuote(String symbol,
                                          CallNotPermittedException cause) {
        return staleQuote(symbol, cause);
    }

    private MarketDataQuote staleQuote(String symbol, RuntimeException cause) {
        log.warn("Market data fallback used for symbol={} cause={}",
                symbol, cause.getClass().getSimpleName());
        return new MarketDataQuote(
                symbol.toUpperCase(Locale.ROOT),
                new BigDecimal("99.00000000"),
                "SIMULATED_STALE_CACHE",
                true,
                Instant.now()
        );
    }
}
