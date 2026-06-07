package com.gustavo.marketflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.gustavo.marketflow.support.PostgreSqlContainerBaseTest;

/**
 * Smoke test: the Spring application context must start cleanly with the
 * default configuration. Catches misconfigured beans, circular
 * dependencies and broken auto-configuration early.
 */
@SpringBootTest
class MarketFlowApplicationTests extends PostgreSqlContainerBaseTest {

    @Test
    void contextLoads() {
        // Intentionally empty. Failure to load the context fails the test.
    }
}
