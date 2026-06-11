package com.gustavo.marketflow.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Returns a safe correlated response when a bearer token is absent or invalid.
 */
@Component
public class BearerAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(BearerAuthenticationEntryPoint.class);

    private final SecurityProblemWriter problemWriter;

    public BearerAuthenticationEntryPoint(SecurityProblemWriter problemWriter) {
        this.problemWriter = problemWriter;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException, ServletException {
        log.warn("Authentication rejected method={} path={}", request.getMethod(), request.getRequestURI());
        response.setHeader("WWW-Authenticate", "Bearer");
        problemWriter.write(
                response,
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication required",
                "A valid bearer token is required to access this resource.",
                "unauthorized"
        );
    }
}
