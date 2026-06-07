package com.gustavo.marketflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MarketFlow Senior Java Cloud Lab - Phase 1 entry point.
 *
 * <p>This module starts the Spring Boot application context which wires
 * the order management vertical slice (controller -> service -> in-memory
 * repository), the global exception handler and the actuator endpoints.</p>
 *
 * <p>Persistence is intentionally in-memory at this phase to keep the first
 * vertical slice executable and focused on Spring Core, REST, validation
 * and exception handling. JPA/PostgreSQL is introduced in Phase 2.</p>
 */
@SpringBootApplication
public class MarketFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketFlowApplication.class, args);
    }
}
