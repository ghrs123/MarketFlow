package com.gustavo.marketflow.monitoring.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gustavo.marketflow.execution.application.DeadLetterQueue;
import com.gustavo.marketflow.execution.application.ExecutionStats;
import com.gustavo.marketflow.execution.application.OrderProcessingEngine;

/**
 * Provides a concise operational view for demonstrations and first-line diagnostics.
 */
@RestController
@RequestMapping("/monitoring")
public class MonitoringSummaryController {

    private final OrderProcessingEngine orderProcessingEngine;
    private final DeadLetterQueue deadLetterQueue;

    public MonitoringSummaryController(OrderProcessingEngine orderProcessingEngine,
                                       DeadLetterQueue deadLetterQueue) {
        this.orderProcessingEngine = orderProcessingEngine;
        this.deadLetterQueue = deadLetterQueue;
    }

    @GetMapping("/summary")
    public MonitoringSummaryResponse summary() {
        ExecutionStats stats = orderProcessingEngine.getStats();
        return new MonitoringSummaryResponse(
                "marketflow-lab",
                stats.running(),
                stats.configuredWorkers(),
                stats.activeWorkers(),
                stats.queueSize(),
                deadLetterQueue.size(),
                stats.totalQueued(),
                stats.totalProcessed(),
                stats.totalSucceeded(),
                stats.totalFailed(),
                stats.timestamp()
        );
    }
}
