package com.gustavo.marketflow.execution.domain;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Bounded in-memory queue for order execution requests.
 *
 * <p>The queue uses {@link BlockingQueue} to model a classic
 * producer-consumer hand-off. A concurrent id index prevents the same order
 * from being enqueued more than once at the same time.</p>
 */
public class OrderQueue {

    private final BlockingQueue<QueuedOrder> queue;
    private final ConcurrentHashMap.KeySetView<UUID, Boolean> queuedOrderIds;

    public OrderQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.queuedOrderIds = ConcurrentHashMap.newKeySet();
    }

    public OrderEnqueueStatus enqueue(QueuedOrder queuedOrder) {
        if (!queuedOrderIds.add(queuedOrder.orderId())) {
            return OrderEnqueueStatus.DUPLICATE;
        }

        boolean offered = queue.offer(queuedOrder);
        if (!offered) {
            queuedOrderIds.remove(queuedOrder.orderId());
            return OrderEnqueueStatus.FULL;
        }

        return OrderEnqueueStatus.ENQUEUED;
    }

    public Optional<QueuedOrder> poll(Duration timeout) throws InterruptedException {
        return Optional.ofNullable(queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS));
    }

    public void markProcessed(UUID orderId) {
        queuedOrderIds.remove(orderId);
    }

    public int size() {
        return queue.size();
    }
}
