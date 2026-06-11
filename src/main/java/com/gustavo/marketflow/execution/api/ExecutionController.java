package com.gustavo.marketflow.execution.api;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.gustavo.marketflow.execution.application.OrderProcessingEngine;
import com.gustavo.marketflow.execution.application.OrderExecutionService;
import com.gustavo.marketflow.execution.domain.DeadLetterMessage;
import com.gustavo.marketflow.resilience.application.BrokerExecutionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

/**
 * REST adapter for the concurrent execution engine.
 *
 * <p>The controller only exposes lifecycle and queue operations. Queue
 * semantics, worker orchestration and processing rules stay in the
 * application layer.</p>
 */
@RestController
@RequestMapping
@SecurityRequirement(name = "bearerAuth")
public class ExecutionController {

    private final OrderProcessingEngine orderProcessingEngine;
    private final OrderExecutionService orderExecutionService;

    public ExecutionController(OrderProcessingEngine orderProcessingEngine,
                               OrderExecutionService orderExecutionService) {
        this.orderProcessingEngine = orderProcessingEngine;
        this.orderExecutionService = orderExecutionService;
    }

    @PostMapping("/orders/{id}/queue")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<Void> queueOrder(@PathVariable UUID id) {
        orderProcessingEngine.enqueue(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/orders/{id}/execute-with-broker")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public BrokerExecutionResult executeWithBroker(@PathVariable UUID id) {
        return orderExecutionService.executeWithBroker(id);
    }

    @PostMapping("/execution/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> start() {
        orderProcessingEngine.start();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/execution/stop")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> stop() {
        orderProcessingEngine.stop();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/execution/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ExecutionStatsResponse stats() {
        return ExecutionStatsResponse.from(orderProcessingEngine.getStats());
    }

    @GetMapping("/execution/dlq")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DeadLetterMessage> deadLetters() {
        return orderProcessingEngine.getDeadLetters();
    }

    @PostMapping("/execution/dlq/{orderId}/reprocess")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reprocessDeadLetter(@PathVariable UUID orderId) {
        orderProcessingEngine.reprocessDeadLetter(orderId);
        return ResponseEntity.accepted().build();
    }
}
