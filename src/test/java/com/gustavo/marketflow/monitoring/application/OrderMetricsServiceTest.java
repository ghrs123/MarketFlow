package com.gustavo.marketflow.monitoring.application;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMetricsServiceTest {

    @Test
    void recordBusinessOutcomes_metricsContainExpectedCountsAndTimer() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OrderMetricsService service = new OrderMetricsService(meterRegistry);

        String result = service.recordCreation(() -> "created");
        service.recordCreated();
        service.recordValidated();
        service.recordRejected();
        service.recordQueued();
        service.recordExecuted();
        service.recordFailed();
        service.recordRetried();
        service.recordDeadLettered();

        assertThat(result).isEqualTo("created");
        assertThat(meterRegistry.get("marketflow.order.creation.duration").timer().count()).isEqualTo(1);
        assertThat(meterRegistry.get("marketflow.orders.created").counter().count()).isEqualTo(1);
        assertThat(meterRegistry.get("marketflow.orders.validated").counter().count()).isEqualTo(1);
        assertThat(meterRegistry.get("marketflow.orders.rejected").counter().count()).isEqualTo(1);
        assertThat(meterRegistry.get("marketflow.orders.queued").counter().count()).isEqualTo(1);
        assertThat(meterRegistry.get("marketflow.orders.executed").counter().count()).isEqualTo(1);
        assertThat(meterRegistry.get("marketflow.orders.failed").counter().count()).isEqualTo(1);
        assertThat(meterRegistry.get("marketflow.orders.retried").counter().count()).isEqualTo(1);
        assertThat(meterRegistry.get("marketflow.orders.dlq").counter().count()).isEqualTo(1);
        meterRegistry.close();
    }
}
