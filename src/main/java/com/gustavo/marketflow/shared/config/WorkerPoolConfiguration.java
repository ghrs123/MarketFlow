package com.gustavo.marketflow.shared.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gustavo.marketflow.execution.application.ExecutionProperties;
import com.gustavo.marketflow.execution.domain.OrderQueue;
import com.gustavo.marketflow.shared.logging.MdcTaskDecorator;
import com.gustavo.marketflow.shared.logging.TaskDecoratingExecutorService;

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
    public ExecutorService executionWorkerExecutor(ExecutionProperties executionProperties,
                                                   MdcTaskDecorator taskDecorator) {
        ExecutorService executorService = Executors.newFixedThreadPool(
                executionProperties.workerCount(),
                namedThreadFactory("execution-worker-")
        );
        return new TaskDecoratingExecutorService(executorService, taskDecorator);
    }

    @Bean(name = "learningExecutor")
    public ExecutorService learningExecutor(MdcTaskDecorator taskDecorator) {
        ExecutorService executorService = Executors.newFixedThreadPool(
                4,
                namedThreadFactory("learning-demo-")
        );
        return new TaskDecoratingExecutorService(executorService, taskDecorator);
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
