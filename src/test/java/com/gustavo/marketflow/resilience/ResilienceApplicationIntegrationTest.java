package com.gustavo.marketflow.resilience;

import com.gustavo.marketflow.execution.application.OrderExecutionService;
import com.gustavo.marketflow.fix.application.FixMessageApplicationService;
import com.gustavo.marketflow.fix.domain.FixMessage;
import com.gustavo.marketflow.order.application.OrderApplicationService;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.order.domain.OrderSide;
import com.gustavo.marketflow.resilience.application.BrokerExecutionResult;
import com.gustavo.marketflow.shared.exception.ExternalServiceUnavailableException;
import com.gustavo.marketflow.shared.exception.OrderNotFoundException;
import com.gustavo.marketflow.support.PostgreSqlContainerBaseTest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that Spring AOP applies the configured resilience policies to application services.
 */
@SpringBootTest
class ResilienceApplicationIntegrationTest extends PostgreSqlContainerBaseTest {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private OrderExecutionService orderExecutionService;

    @Autowired
    private FixMessageApplicationService fixMessageApplicationService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        circuitBreakerRegistry.circuitBreaker("broker").reset();
    }

    @Test
    void executeWithBroker_dependencyFailure_returnsFallback() {
        Order order = createOrder("BROKER_FAIL");

        BrokerExecutionResult result = orderExecutionService.executeWithBroker(order.getId());

        assertThat(result.fallback()).isTrue();
        assertThat(result.status()).isEqualTo("PENDING_BROKER_RECOVERY");
    }

    @Test
    void executeWithBroker_missingOrder_preservesDomainError() {
        UUID missingOrderId = UUID.randomUUID();

        assertThatThrownBy(() -> orderExecutionService.executeWithBroker(missingOrderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(missingOrderId.toString());
    }

    @Test
    void generateFix_transientFailures_retriesAndPersistsMessage() {
        Order order = createOrder("FIX_RETRY");

        FixMessage result = fixMessageApplicationService.generateForOrder(order.getId());

        assertThat(result.orderId()).isEqualTo(order.getId());
        assertThat(retryRegistry.retry("fixMessage")
                .getMetrics()
                .getNumberOfSuccessfulCallsWithRetryAttempt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void generateFix_persistentFailure_returnsServiceUnavailable() {
        Order order = createOrder("FIX_FAIL");

        assertThatThrownBy(() -> fixMessageApplicationService.generateForOrder(order.getId()))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessageContaining("FIX message service");
    }

    private Order createOrder(String symbol) {
        return orderApplicationService.createOrder(
                "C-RESILIENCE",
                symbol,
                OrderSide.BUY,
                new BigDecimal("10.00000000"),
                new BigDecimal("100.00000000"),
                UUID.randomUUID().toString()
        );
    }
}
