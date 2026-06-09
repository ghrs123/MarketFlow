package com.gustavo.marketflow.learning.concurrency;

import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates lock-free shared-counter updates with {@link AtomicInteger}.
 */
@Component
public class AtomicCounterDemo {

    public RaceConditionDemoResult run(int threadCount, int incrementsPerThread) {
        AtomicInteger counter = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount, namedThreadFactory());
        CountDownLatch done = new CountDownLatch(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.incrementAndGet();
                    }
                    done.countDown();
                });
            }
            await(done);
        } finally {
            shutdown(executorService);
        }
        return new RaceConditionDemoResult(threadCount * incrementsPerThread, counter.get(), "atomic");
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Atomic demo interrupted", ex);
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

    private ThreadFactory namedThreadFactory() {
        AtomicInteger threadCounter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("atomic-demo-" + threadCounter.getAndIncrement());
            return thread;
        };
    }
}
