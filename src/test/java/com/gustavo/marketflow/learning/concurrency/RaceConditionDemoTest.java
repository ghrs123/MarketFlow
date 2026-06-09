package com.gustavo.marketflow.learning.concurrency;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RaceConditionDemoTest {

    @Test
    void run_underContention_losesUpdates() {
        RaceConditionDemo demo = new RaceConditionDemo();

        RaceConditionDemoResult result = demo.run(16, 5_000);

        assertThat(result.actual()).isLessThan(result.expected());
    }
}
