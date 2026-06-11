package com.gustavo.marketflow.security;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedUserMdcFilterTest {

    private final AuthenticatedUserMdcFilter filter = new AuthenticatedUserMdcFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void doFilter_authenticatedPrincipal_addsAndRestoresUserId() throws Exception {
        AtomicReference<String> observedUserId = new AtomicReference<>();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("trader-1", "N/A", List.of()));
        MDC.put("userId", "previous-user");

        filter.doFilter(
                new MockHttpServletRequest("GET", "/orders"),
                new MockHttpServletResponse(),
                (request, response) -> observedUserId.set(MDC.get("userId"))
        );

        assertThat(observedUserId.get()).isEqualTo("trader-1");
        assertThat(MDC.get("userId")).isEqualTo("previous-user");
    }
}
