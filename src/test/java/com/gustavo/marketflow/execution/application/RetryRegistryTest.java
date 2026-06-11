package com.gustavo.marketflow.execution.application;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RetryRegistryTest {

    @Test
    void recordAttempt_concurrentCalls_retainsEveryAttempt() throws Exception {
        RetryRegistry registry = new RetryRegistry();
        UUID orderId = UUID.randomUUID();
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            for (int i = 0; i < 100; i++) {
                executor.submit(() -> registry.recordAttempt(orderId));
            }
            executor.shutdown();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdownNow();
        }

        assertThat(registry.attemptsFor(orderId)).isEqualTo(100);
    }

    @Test
    void clear_existingAttempts_resetsCounter() {
        RetryRegistry registry = new RetryRegistry();
        UUID orderId = UUID.randomUUID();
        registry.recordAttempt(orderId);

        registry.clear(orderId);

        assertThat(registry.attemptsFor(orderId)).isZero();
    }
}
