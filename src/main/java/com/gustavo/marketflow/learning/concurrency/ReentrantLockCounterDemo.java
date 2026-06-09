package com.gustavo.marketflow.learning.concurrency;

import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates explicit lock-based protection of a shared counter.
 */
@Component
public class ReentrantLockCounterDemo {

    public RaceConditionDemoResult run(int threadCount, int incrementsPerThread) {
        LockingCounter counter = new LockingCounter();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount, namedThreadFactory());
        CountDownLatch done = new CountDownLatch(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                    done.countDown();
                });
            }
            await(done);
        } finally {
            shutdown(executorService);
        }
        return new RaceConditionDemoResult(threadCount * incrementsPerThread, counter.get(), "reentrant-lock");
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ReentrantLock demo interrupted", ex);
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
            thread.setName("lock-demo-" + threadCounter.getAndIncrement());
            return thread;
        };
    }

    private static final class LockingCounter {

        private final ReentrantLock lock;
        private int value;

        private LockingCounter() {
            this.lock = new ReentrantLock();
        }

        void increment() {
            // Protects the invariant that reading and incrementing happen atomically.
            lock.lock();
            try {
                value++;
            } finally {
                lock.unlock();
            }
        }

        int get() {
            // Protects the invariant that reads observe a fully-updated value.
            lock.lock();
            try {
                return value;
            } finally {
                lock.unlock();
            }
        }
    }
}
