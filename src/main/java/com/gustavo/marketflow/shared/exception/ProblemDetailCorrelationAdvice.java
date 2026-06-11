package com.gustavo.marketflow.shared.exception;

import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Adds the active correlation identifier to every RFC 7807 response.
 */
@RestControllerAdvice
public class ProblemDetailCorrelationAdvice implements ResponseBodyAdvice<ProblemDetail> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return ProblemDetail.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public ProblemDetail beforeBodyWrite(ProblemDetail body,
                                         MethodParameter returnType,
                                         MediaType selectedContentType,
                                         Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                         ServerHttpRequest request,
                                         ServerHttpResponse response) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            body.setProperty("correlationId", correlationId);
        }
        return body;
    }
}
