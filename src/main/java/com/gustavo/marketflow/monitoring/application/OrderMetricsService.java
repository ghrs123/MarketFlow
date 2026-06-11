package com.gustavo.marketflow.monitoring.application;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

/**
 * Centralizes order business metrics so metric names remain stable across use cases.
 */
@Service
public class OrderMetricsService {

    private final Counter createdCounter;
    private final Counter validatedCounter;
    private final Counter rejectedCounter;
    private final Counter queuedCounter;
    private final Counter executedCounter;
    private final Counter failedCounter;
    private final Counter retriedCounter;
    private final Counter deadLetterCounter;
    private final Timer creationTimer;
    private final AtomicInteger activeClientSessions;

    public OrderMetricsService(MeterRegistry meterRegistry) {
        this.createdCounter = meterRegistry.counter("marketflow.orders.created");
        this.validatedCounter = meterRegistry.counter("marketflow.orders.validated");
        this.rejectedCounter = meterRegistry.counter("marketflow.orders.rejected");
        this.queuedCounter = meterRegistry.counter("marketflow.orders.queued");
        this.executedCounter = meterRegistry.counter("marketflow.orders.executed");
        this.failedCounter = meterRegistry.counter("marketflow.orders.failed");
        this.retriedCounter = meterRegistry.counter("marketflow.orders.retried");
        this.deadLetterCounter = meterRegistry.counter("marketflow.orders.dlq");
        this.creationTimer = meterRegistry.timer("marketflow.order.creation.duration");
        this.activeClientSessions = new AtomicInteger();
        meterRegistry.gauge("marketflow.active.client.sessions", activeClientSessions);
    }

    public <T> T recordCreation(Supplier<T> operation) {
        return creationTimer.record(operation);
    }

    public void recordCreated() {
        createdCounter.increment();
    }

    public void recordValidated() {
        validatedCounter.increment();
    }

    public void recordRejected() {
        rejectedCounter.increment();
    }

    public void recordQueued() {
        queuedCounter.increment();
    }

    public void recordExecuted() {
        executedCounter.increment();
    }

    public void recordFailed() {
        failedCounter.increment();
    }

    public void recordRetried() {
        retriedCounter.increment();
    }

    public void recordDeadLettered() {
        deadLetterCounter.increment();
    }
}
