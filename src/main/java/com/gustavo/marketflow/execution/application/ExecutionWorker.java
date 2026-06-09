package com.gustavo.marketflow.execution.application;

import com.gustavo.marketflow.execution.domain.OrderQueue;
import com.gustavo.marketflow.execution.domain.QueuedOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Long-running worker that drains the internal order queue.
 *
 * <p>The worker polls with a timeout so it can observe shutdown requests
 * without blocking forever on an empty queue.</p>
 */
public class ExecutionWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ExecutionWorker.class);
    private static final Duration POLL_TIMEOUT = Duration.ofMillis(250);

    private final AtomicBoolean running;
    private final OrderQueue orderQueue;
    private final OrderProcessingEngine orderProcessingEngine;

    public ExecutionWorker(AtomicBoolean running,
                           OrderQueue orderQueue,
                           OrderProcessingEngine orderProcessingEngine) {
        this.running = running;
        this.orderQueue = orderQueue;
        this.orderProcessingEngine = orderProcessingEngine;
    }

    @Override
    public void run() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Optional<QueuedOrder> queuedOrder = orderQueue.poll(POLL_TIMEOUT);
                if (queuedOrder.isEmpty()) {
                    continue;
                }
                withMdc(queuedOrder.get().mdcContext(), () -> orderProcessingEngine.handleQueuedOrder(queuedOrder.get()));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.debug("Execution worker interrupted during shutdown");
            } catch (RuntimeException ex) {
                log.error("Unexpected worker failure", ex);
            }
        }
    }

    private void withMdc(Map<String, String> contextMap, Runnable action) {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        try {
            if (contextMap == null || contextMap.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(contextMap);
            }
            action.run();
        } finally {
            if (previousContext == null || previousContext.isEmpty()) {
                MDC.clear();
            } else {
                MDC.setContextMap(previousContext);
            }
        }
    }
}
