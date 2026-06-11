package com.gustavo.marketflow.resilience.infrastructure;

import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.resilience.application.ResilienceProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulates an external broker with deterministic success, failure and timeout modes.
 */
@Component
public class BrokerClient {

    private static final String FAILURE_SYMBOL = "BROKER_FAIL";
    private static final String TIMEOUT_SYMBOL = "BROKER_TIMEOUT";

    private final ResilienceProperties properties;

    public BrokerClient(ResilienceProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns a broker reference or raises a transient simulated dependency failure.
     */
    public String execute(Order order) {
        if (FAILURE_SYMBOL.equalsIgnoreCase(order.getSymbol())) {
            throw new TransientExternalServiceException("Simulated broker failure");
        }
        if (TIMEOUT_SYMBOL.equalsIgnoreCase(order.getSymbol())) {
            pause(properties.brokerTimeoutMillis());
            throw new TransientExternalServiceException("Simulated broker timeout");
        }
        return "BRK-" + UUID.randomUUID();
    }

    private void pause(long delayMillis) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new TransientExternalServiceException("Broker call interrupted");
        }
    }
}
