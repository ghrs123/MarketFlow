package com.gustavo.marketflow.shared.logging;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.core.task.TaskDecorator;

/**
 * Applies a Spring {@link TaskDecorator} while retaining the lifecycle contract required by ExecutorService clients.
 */
public class TaskDecoratingExecutorService extends AbstractExecutorService {

    private final ExecutorService delegate;
    private final TaskDecorator taskDecorator;

    public TaskDecoratingExecutorService(ExecutorService delegate, TaskDecorator taskDecorator) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.taskDecorator = Objects.requireNonNull(taskDecorator, "taskDecorator");
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(taskDecorator.decorate(command));
    }
}
