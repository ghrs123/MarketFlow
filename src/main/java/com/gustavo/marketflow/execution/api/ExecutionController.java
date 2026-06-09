package com.gustavo.marketflow.execution.api;

import com.gustavo.marketflow.execution.application.OrderProcessingEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST adapter for the concurrent execution engine.
 *
 * <p>The controller only exposes lifecycle and queue operations. Queue
 * semantics, worker orchestration and processing rules stay in the
 * application layer.</p>
 */
@RestController
@RequestMapping
public class ExecutionController {

    private final OrderProcessingEngine orderProcessingEngine;

    public ExecutionController(OrderProcessingEngine orderProcessingEngine) {
        this.orderProcessingEngine = orderProcessingEngine;
    }

    @PostMapping("/orders/{id}/queue")
    public ResponseEntity<Void> queueOrder(@PathVariable UUID id) {
        orderProcessingEngine.enqueue(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/execution/start")
    public ResponseEntity<Void> start() {
        orderProcessingEngine.start();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/execution/stop")
    public ResponseEntity<Void> stop() {
        orderProcessingEngine.stop();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/execution/stats")
    public ExecutionStatsResponse stats() {
        return ExecutionStatsResponse.from(orderProcessingEngine.getStats());
    }
}
