package com.gustavo.marketflow.monitoring.api;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.gustavo.marketflow.execution.application.DeadLetterQueue;
import com.gustavo.marketflow.execution.application.ExecutionStats;
import com.gustavo.marketflow.execution.application.OrderProcessingEngine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonitoringSummaryControllerTest {

    @Test
    void summary_engineSnapshot_returnsOperationalResponse() {
        OrderProcessingEngine engine = mock(OrderProcessingEngine.class);
        DeadLetterQueue deadLetterQueue = new DeadLetterQueue();
        Instant timestamp = Instant.parse("2026-06-11T10:00:00Z");
        when(engine.getStats()).thenReturn(new ExecutionStats(
                true, 4, 3, 2, 10, 8, 7, 1, timestamp));
        MonitoringSummaryController controller = new MonitoringSummaryController(engine, deadLetterQueue);

        MonitoringSummaryResponse response = controller.summary();

        assertThat(response.service()).isEqualTo("marketflow-lab");
        assertThat(response.engineRunning()).isTrue();
        assertThat(response.activeWorkers()).isEqualTo(3);
        assertThat(response.queueSize()).isEqualTo(2);
        assertThat(response.totalFailed()).isEqualTo(1);
        assertThat(response.timestamp()).isEqualTo(timestamp);
    }
}
