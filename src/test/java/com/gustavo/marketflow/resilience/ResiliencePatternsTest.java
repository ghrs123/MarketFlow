package com.gustavo.marketflow.resilience;

import com.gustavo.marketflow.order.OrderTestData;
import com.gustavo.marketflow.order.domain.Order;
import com.gustavo.marketflow.resilience.application.ResilienceProperties;
import com.gustavo.marketflow.resilience.infrastructure.BrokerClient;
import com.gustavo.marketflow.resilience.infrastructure.FixGenerationAvailability;
import com.gustavo.marketflow.resilience.infrastructure.TransientExternalServiceException;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResiliencePatternsTest {

    @Test
    void circuitBreaker_repeatedBrokerFailures_opensAndRejectsCalls() {
        BrokerClient brokerClient = new BrokerClient(new ResilienceProperties(10, 2));
        Order order = OrderTestData.withSymbol("BROKER_FAIL");
        CircuitBreaker circuitBreaker = CircuitBreaker.of(
                "broker-test",
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(3)
                        .minimumNumberOfCalls(3)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .build()
        );

        for (int attempt = 0; attempt < 3; attempt++) {
            assertThatThrownBy(() -> circuitBreaker.executeSupplier(() -> brokerClient.execute(order)))
                    .isInstanceOf(TransientExternalServiceException.class);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThatThrownBy(() -> circuitBreaker.executeSupplier(() -> brokerClient.execute(order)))
                .isInstanceOf(CallNotPermittedException.class);
    }

    @Test
    void retry_transientFixFailures_succeedsOnThirdAttempt() {
        FixGenerationAvailability availability = new FixGenerationAvailability(
                new ResilienceProperties(10, 2)
        );
        Order order = OrderTestData.withSymbol("FIX_RETRY");
        Retry retry = Retry.of(
                "fix-test",
                RetryConfig.custom()
                        .maxAttempts(3)
                        .waitDuration(Duration.ZERO)
                        .retryExceptions(TransientExternalServiceException.class)
                        .build()
        );

        assertThatCode(() -> retry.executeRunnable(() -> availability.assertAvailable(order)))
                .doesNotThrowAnyException();
        assertThat(retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(1);
    }

    @Test
    void rateLimiter_budgetExhausted_rejectsNextPermission() {
        RateLimiter rateLimiter = RateLimiter.of(
                "orders-test",
                RateLimiterConfig.custom()
                        .limitForPeriod(2)
                        .limitRefreshPeriod(Duration.ofMinutes(1))
                        .timeoutDuration(Duration.ZERO)
                        .build()
        );

        assertThat(rateLimiter.acquirePermission()).isTrue();
        assertThat(rateLimiter.acquirePermission()).isTrue();
        assertThat(rateLimiter.acquirePermission()).isFalse();
    }

    @Test
    void bulkhead_capacityOccupied_rejectsConcurrentCall() {
        Bulkhead bulkhead = Bulkhead.of(
                "processing-test",
                BulkheadConfig.custom()
                        .maxConcurrentCalls(1)
                        .maxWaitDuration(Duration.ZERO)
                        .build()
        );
        assertThat(bulkhead.tryAcquirePermission()).isTrue();

        assertThatThrownBy(() -> Bulkhead.decorateRunnable(bulkhead, () -> { }).run())
                .isInstanceOf(BulkheadFullException.class);

        bulkhead.releasePermission();
        assertThat(bulkhead.getMetrics().getAvailableConcurrentCalls()).isEqualTo(1);
    }
}
