package com.gustavo.marketflow.execution.application;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionStatisticsTest {

    @Test
    void snapshot_concurrentUpdates_keepsCountsConsistent() throws Exception {
        ExecutionStatistics executionStatistics = new ExecutionStatistics();
        ExecutorService executorService = Executors.newFixedThreadPool(8, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("stats-test-" + thread.threadId());
            return thread;
        });
        CountDownLatch done = new CountDownLatch(20);
        try {
            for (int i = 0; i < 20; i++) {
                int taskIndex = i;
                executorService.submit(() -> {
                    executionStatistics.recordQueued();
                    executionStatistics.recordProcessed(taskIndex % 2 == 0);
                    done.countDown();
                });
            }

            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();

            ExecutionStats snapshot = executionStatistics.snapshot(true, 4, 4, 0);

            assertThat(snapshot.totalQueued()).isEqualTo(20);
            assertThat(snapshot.totalProcessed()).isEqualTo(20);
            assertThat(snapshot.totalSucceeded()).isEqualTo(10);
            assertThat(snapshot.totalFailed()).isEqualTo(10);
        } finally {
            executorService.shutdownNow();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
