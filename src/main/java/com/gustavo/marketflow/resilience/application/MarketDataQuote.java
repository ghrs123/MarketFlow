package com.gustavo.marketflow.resilience.application;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Simulated market-data quote returned by the external dependency adapter.
 */
public record MarketDataQuote(
        String symbol,
        BigDecimal price,
        String source,
        boolean fallback,
        Instant observedAt
) {
}
