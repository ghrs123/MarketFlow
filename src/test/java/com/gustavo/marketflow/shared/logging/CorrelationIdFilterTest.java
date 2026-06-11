package com.gustavo.marketflow.shared.logging;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final CorrelationIdFilter filter = new CorrelationIdFilter(meterRegistry);

    @AfterEach
    void tearDown() {
        MDC.clear();
        meterRegistry.close();
    }

    @Test
    void doFilter_validCorrelationId_propagatesHeaderAndRecordsMetric() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .isEqualTo("request-123");
        assertThat(meterRegistry.get("marketflow.http.server.requests").timer().count()).isEqualTo(1);
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void doFilter_invalidCorrelationId_generatesSafeIdentifier() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "invalid value with spaces");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .matches("[0-9a-f-]{36}")
                .isNotEqualTo("invalid value with spaces");
    }
}
