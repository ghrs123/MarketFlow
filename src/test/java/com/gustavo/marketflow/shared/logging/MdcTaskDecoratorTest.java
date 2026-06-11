package com.gustavo.marketflow.shared.logging;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class MdcTaskDecoratorTest {

    private final MdcTaskDecorator taskDecorator = new MdcTaskDecorator();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void decorate_capturedContext_propagatesAndRestoresWorkerContext() {
        AtomicReference<String> observedCorrelationId = new AtomicReference<>();
        MDC.put("correlationId", "captured-request");
        Runnable decorated = taskDecorator.decorate(
                () -> observedCorrelationId.set(MDC.get("correlationId")));

        MDC.put("correlationId", "worker-existing");
        decorated.run();

        assertThat(observedCorrelationId.get()).isEqualTo("captured-request");
        assertThat(MDC.get("correlationId")).isEqualTo("worker-existing");
    }
}
