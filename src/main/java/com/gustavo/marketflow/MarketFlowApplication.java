package com.gustavo.marketflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MarketFlow Senior Java Cloud Lab entry point.
 *
 * <p>This module starts the Spring Boot application context which wires
 * the order management vertical slice (controller -> service -> JPA
 * repositories), the global exception handler and the actuator endpoints.</p>
 *
 * <p>Orders and their history are persisted in PostgreSQL. Flyway owns schema
 * evolution and the application service defines the transactional boundary.</p>
 */
@SpringBootApplication
public class MarketFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketFlowApplication.class, args);
    }
}
