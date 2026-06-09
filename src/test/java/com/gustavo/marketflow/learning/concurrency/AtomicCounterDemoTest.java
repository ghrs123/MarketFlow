package com.gustavo.marketflow.learning.concurrency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AtomicCounterDemoTest {

    @Test
    void run_underContention_keepsExpectedCount() {
        AtomicCounterDemo demo = new AtomicCounterDemo();

        RaceConditionDemoResult result = demo.run(16, 5_000);

        assertThat(result.actual()).isEqualTo(result.expected());
    }
}
