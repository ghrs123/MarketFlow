package com.gustavo.marketflow.learning.concurrency;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Didactic deadlock explanation used by the learning endpoint.
 *
 * <p>The demo intentionally does not create a real deadlock in tests or at
 * runtime because hanging worker threads would make the application flaky.
 * It documents the lock-ordering problem and the fix instead.</p>
 */
@Component
public class DeadlockDemo {

    public Map<String, Object> describe() {
        return Map.of(
                "topic", "Deadlock",
                "structure", "Nested locks with inconsistent ordering",
                "whyThisStructure", "Deadlocks are easiest to explain when two threads acquire the same locks in opposite order.",
                "tradeOffs", List.of(
                        "The endpoint documents the scenario instead of creating a real deadlock that would hang the process.",
                        "The real fix is global lock ordering or reducing shared mutable state."
                ),
                "interviewTopics", List.of(
                        "What causes a deadlock?",
                        "How do you detect deadlocks in production?",
                        "How do consistent lock-ordering rules prevent deadlocks?"
                )
        );
    }
}
