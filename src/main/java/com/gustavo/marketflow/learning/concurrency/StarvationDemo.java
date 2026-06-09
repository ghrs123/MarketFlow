package com.gustavo.marketflow.learning.concurrency;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Didactic starvation explanation used by the learning material.
 */
@Component
public class StarvationDemo {

    public Map<String, Object> describe() {
        return Map.of(
                "topic", "Starvation",
                "structure", "Fixed thread pool with unfair scheduling pressure",
                "whyThisStructure", "Starvation is usually about work never getting CPU or lock access because other work keeps winning.",
                "tradeOffs", List.of(
                        "A simple fixed pool makes the concept easier to explain than a production-grade scheduler.",
                        "The endpoint stays descriptive because a real starvation demo would be timing-sensitive and flaky."
                ),
                "interviewTopics", List.of(
                        "What is starvation and how is it different from deadlock?",
                        "How can an unfair lock or saturated thread pool create starvation?",
                        "What metrics help detect starvation in production?"
                )
        );
    }
}
