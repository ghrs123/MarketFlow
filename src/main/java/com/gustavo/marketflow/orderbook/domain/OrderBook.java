package com.gustavo.marketflow.orderbook.domain;

import com.gustavo.marketflow.order.domain.OrderSide;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains an in-memory bid/ask book ordered by price.
 *
 * <p>BUY orders are ordered by highest price first. SELL orders are ordered by
 * lowest price first. A side map is kept for direct membership checks so the
 * same order cannot be inserted twice.</p>
 */
public class OrderBook {

    private static final Comparator<OrderTask> BUY_COMPARATOR = Comparator
            .comparing(OrderTask::price, Comparator.reverseOrder())
            .thenComparing(OrderTask::createdAt)
            .thenComparing(OrderTask::orderId);

    private static final Comparator<OrderTask> SELL_COMPARATOR = Comparator
            .comparing(OrderTask::price)
            .thenComparing(OrderTask::createdAt)
            .thenComparing(OrderTask::orderId);

    private final PriorityQueue<OrderTask> buyOrders;
    private final PriorityQueue<OrderTask> sellOrders;
    private final ConcurrentHashMap<UUID, OrderTask> orderIndex;

    public OrderBook() {
        this.buyOrders = new PriorityQueue<>(BUY_COMPARATOR);
        this.sellOrders = new PriorityQueue<>(SELL_COMPARATOR);
        this.orderIndex = new ConcurrentHashMap<>();
    }

    /**
     * Adds an order to the appropriate side of the book.
     *
     * @param orderTask immutable item to index and order in memory
     * @return true when the order was added, false when the id already exists
     */
    public boolean add(OrderTask orderTask) {
        OrderTask existing = orderIndex.putIfAbsent(orderTask.orderId(), orderTask);
        if (existing != null) {
            return false;
        }

        // Protects the invariant that queues and orderIndex stay in sync.
        synchronized (this) {
            queueFor(orderTask.side()).add(orderTask);
        }
        return true;
    }

    public Optional<OrderTask> bestBuy() {
        // Protects consistent queue head reads while a mutation is in progress.
        synchronized (this) {
            return Optional.ofNullable(buyOrders.peek());
        }
    }

    public Optional<OrderTask> bestSell() {
        // Protects consistent queue head reads while a mutation is in progress.
        synchronized (this) {
            return Optional.ofNullable(sellOrders.peek());
        }
    }

    public Optional<OrderTask> findByOrderId(UUID orderId) {
        return Optional.ofNullable(orderIndex.get(orderId));
    }

    public List<OrderTask> buySnapshot() {
        // Protects queue-copy reads from interleaving with queue mutations.
        synchronized (this) {
            PriorityQueue<OrderTask> copy = new PriorityQueue<>(buyOrders);
            List<OrderTask> snapshot = new ArrayList<>(copy.size());
            while (!copy.isEmpty()) {
                snapshot.add(copy.poll());
            }
            return snapshot;
        }
    }

    public List<OrderTask> sellSnapshot() {
        // Protects queue-copy reads from interleaving with queue mutations.
        synchronized (this) {
            PriorityQueue<OrderTask> copy = new PriorityQueue<>(sellOrders);
            List<OrderTask> snapshot = new ArrayList<>(copy.size());
            while (!copy.isEmpty()) {
                snapshot.add(copy.poll());
            }
            return snapshot;
        }
    }

    public int size() {
        return orderIndex.size();
    }

    private PriorityQueue<OrderTask> queueFor(OrderSide side) {
        return side == OrderSide.BUY ? buyOrders : sellOrders;
    }
}
