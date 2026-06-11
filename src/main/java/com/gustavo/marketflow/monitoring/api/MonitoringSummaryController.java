package com.gustavo.marketflow.monitoring.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.gustavo.marketflow.execution.application.DeadLetterQueue;
import com.gustavo.marketflow.execution.application.ExecutionStats;
import com.gustavo.marketflow.execution.application.OrderProcessingEngine;

/**
 * Provides a concise operational view for demonstrations and first-line diagnostics.
 */
@RestController
@RequestMapping("/monitoring")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
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
