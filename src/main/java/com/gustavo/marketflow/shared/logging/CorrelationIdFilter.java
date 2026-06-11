package com.gustavo.marketflow.shared.logging;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ensures every inbound HTTP request has a correlation identifier.
 *
 * <p>The filter reads {@code X-Correlation-Id} when provided, otherwise it
 * generates a UUID, stores it in MDC and echoes it back on the response so
 * downstream logs and clients can correlate the request lifecycle.</p>
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final Pattern VALID_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._:-]{1,128}");

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final MeterRegistry meterRegistry;

    public CorrelationIdFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request.getHeader(CORRELATION_ID_HEADER));
        Timer.Sample sample = Timer.start(meterRegistry);
        MDC.put("correlationId", correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        log.info("HTTP request started method={} path={}", request.getMethod(), request.getRequestURI());
        try {
            filterChain.doFilter(request, response);
        } finally {
            sample.stop(Timer.builder("marketflow.http.server.requests")
                    .description("Duration of inbound HTTP requests")
                    .tag("method", request.getMethod())
                    .tag("status", Integer.toString(response.getStatus()))
                    .register(meterRegistry));
            log.info("HTTP request completed method={} path={} status={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus());
            MDC.remove("correlationId");
        }
    }

    private String resolveCorrelationId(String requestedCorrelationId) {
        if (requestedCorrelationId != null
                && VALID_CORRELATION_ID.matcher(requestedCorrelationId).matches()) {
            return requestedCorrelationId;
        }
        return UUID.randomUUID().toString();
    }
}
