package com.gustavo.marketflow.shared.config;

import com.gustavo.marketflow.execution.application.ExecutionProperties;
import com.gustavo.marketflow.execution.domain.OrderQueue;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Registers queue and worker-pool infrastructure for Phase 5.
 *
 * <p>The execution engine must never run on the common pool because worker
 * lifecycle and thread naming are part of the application contract in this
 * phase.</p>
 */
@Configuration
@EnableConfigurationProperties(ExecutionProperties.class)
public class WorkerPoolConfiguration {

    @Bean
    public OrderQueue orderQueue(ExecutionProperties executionProperties) {
        return new OrderQueue(executionProperties.queueCapacity());
    }

    @Bean(name = "executionWorkerExecutor")
    public ExecutorService executionWorkerExecutor(ExecutionProperties executionProperties) {
        return Executors.newFixedThreadPool(
                executionProperties.workerCount(),
                namedThreadFactory("execution-worker-")
        );
    }

    @Bean(name = "learningExecutor")
    public ExecutorService learningExecutor() {
        return Executors.newFixedThreadPool(4, namedThreadFactory("learning-demo-"));
    }

    private ThreadFactory namedThreadFactory(String prefix) {
        AtomicInteger threadCounter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + threadCounter.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
    }
}
