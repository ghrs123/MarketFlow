package com.gustavo.marketflow.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Returns a safe correlated response when an authenticated principal lacks authority.
 */
@Component
public class BearerAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(BearerAccessDeniedHandler.class);

    private final SecurityProblemWriter problemWriter;

    public BearerAccessDeniedHandler(SecurityProblemWriter problemWriter) {
        this.problemWriter = problemWriter;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("Authorization denied method={} path={}", request.getMethod(), request.getRequestURI());
        problemWriter.write(
                response,
                HttpStatus.FORBIDDEN.value(),
                "Access denied",
                "The authenticated principal does not have permission to access this resource.",
                "forbidden"
        );
    }
}
