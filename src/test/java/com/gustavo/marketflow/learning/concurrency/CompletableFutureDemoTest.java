package com.gustavo.marketflow.learning.concurrency;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CompletableFutureDemoTest {

    private ExecutorService executorService;

    @AfterEach
    void tearDown() throws Exception {
        MDC.clear();
        if (executorService != null) {
            executorService.shutdownNow();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void run_usesNamedExecutorAndPropagatesMdc() {
        AtomicInteger threadCounter = new AtomicInteger(1);
        executorService = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("cf-test-" + threadCounter.getAndIncrement());
            return thread;
        });
        CompletableFutureDemo demo = new CompletableFutureDemo(executorService);
        MDC.put("correlationId", "cf-correlation");

        CompletableFutureDemoResult result = demo.run("cf-correlation");

        assertThat(result.result()).isEqualTo("queued-processed");
        assertThat(result.mdcPropagated()).isTrue();
        assertThat(result.threadNames()).allMatch(threadName -> threadName.startsWith("cf-test-"));
    }
}
