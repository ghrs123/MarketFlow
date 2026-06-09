package com.gustavo.marketflow.learning.concurrency;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates lost updates when shared mutable state is incremented without
 * synchronization.
 */
@Component
public class RaceConditionDemo {

    public RaceConditionDemoResult run(int threadCount, int incrementsPerThread) {
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        execute(threadCount, incrementsPerThread, unsafeCounter::increment, "race-demo-");
        return new RaceConditionDemoResult(threadCount * incrementsPerThread, unsafeCounter.get(), "unsafe");
    }

    private void execute(int threadCount,
                         int incrementsPerThread,
                         Runnable incrementAction,
                         String threadPrefix) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount, namedThreadFactory(threadPrefix));
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    ready.countDown();
                    await(start);
                    for (int j = 0; j < incrementsPerThread; j++) {
                        incrementAction.run();
                    }
                    done.countDown();
                });
            }
            await(ready);
            start.countDown();
            await(done);
        } finally {
            shutdown(executorService);
        }
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Concurrency demo interrupted", ex);
        }
    }

    private void shutdown(ExecutorService executorService) {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private ThreadFactory namedThreadFactory(String prefix) {
        AtomicInteger threadCounter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + threadCounter.getAndIncrement());
            return thread;
        };
    }

    private static final class UnsafeCounter {

        private int value;

        void increment() {
            int current = value;
            Thread.yield();
            value = current + 1;
        }

        int get() {
            return value;
        }
    }
}
