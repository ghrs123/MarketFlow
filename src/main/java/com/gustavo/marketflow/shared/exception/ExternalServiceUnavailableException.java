package com.gustavo.marketflow.shared.exception;

/**
 * Represents exhausted resilience handling for an unavailable dependency.
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    private final String serviceName;

    public ExternalServiceUnavailableException(String serviceName) {
        super(serviceName + " is temporarily unavailable");
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
