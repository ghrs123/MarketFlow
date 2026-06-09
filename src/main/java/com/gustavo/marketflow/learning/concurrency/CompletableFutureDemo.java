package com.gustavo.marketflow.learning.concurrency;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates CompletableFuture execution on an application-managed pool
 * with manual MDC propagation.
 */
@Component
public class CompletableFutureDemo {

    private final ExecutorService learningExecutor;

    public CompletableFutureDemo(@Qualifier("learningExecutor") ExecutorService learningExecutor) {
        this.learningExecutor = learningExecutor;
    }

    public CompletableFutureDemoResult run(String expectedCorrelationId) {
        AtomicReference<String> firstThread = new AtomicReference<>();
        AtomicReference<String> secondThread = new AtomicReference<>();
        AtomicReference<Boolean> propagated = new AtomicReference<>(Boolean.FALSE);
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        CompletableFuture<String> future = CompletableFuture
                .supplyAsync(withMdc(contextMap, () -> {
                    firstThread.set(Thread.currentThread().getName());
                    return "queued";
                }), learningExecutor)
                .thenApplyAsync(withMdc(contextMap, value -> {
                    secondThread.set(Thread.currentThread().getName());
                    propagated.set(expectedCorrelationId.equals(MDC.get("correlationId")));
                    return value + "-processed";
                }), learningExecutor);

        return new CompletableFutureDemoResult(
                future.join(),
                List.of(firstThread.get(), secondThread.get()),
                propagated.get()
        );
    }

    private <T> java.util.function.Supplier<T> withMdc(Map<String, String> contextMap,
                                                       java.util.function.Supplier<T> supplier) {
        return () -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();
            try {
                if (contextMap == null || contextMap.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(contextMap);
                }
                return supplier.get();
            } finally {
                if (previousContext == null || previousContext.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousContext);
                }
            }
        };
    }

    private <T, R> java.util.function.Function<T, R> withMdc(Map<String, String> contextMap,
                                                             java.util.function.Function<T, R> function) {
        return value -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();
            try {
                if (contextMap == null || contextMap.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(contextMap);
                }
                return function.apply(value);
            } finally {
                if (previousContext == null || previousContext.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousContext);
                }
            }
        };
    }
}
