package com.gustavo.marketflow.resilience.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simulates a fire-and-forget external notification after broker acceptance.
 */
@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    /**
     * Records the simulated notification without introducing another failure point.
     */
    public void notifyBrokerAccepted(UUID orderId, String brokerReference) {
        log.info("Broker acceptance notification simulated orderId={} brokerReference={}",
                orderId, brokerReference);
    }
}
