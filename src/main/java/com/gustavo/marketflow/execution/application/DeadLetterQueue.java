package com.gustavo.marketflow.execution.application;

import com.gustavo.marketflow.execution.domain.DeadLetterMessage;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-local dead-letter queue keyed by order identifier.
 */
@Component
public class DeadLetterQueue {

    private final ConcurrentHashMap<UUID, DeadLetterMessage> messagesByOrder;

    public DeadLetterQueue() {
        this.messagesByOrder = new ConcurrentHashMap<>();
    }

    public void add(DeadLetterMessage message) {
        messagesByOrder.put(message.orderId(), message);
    }

    public List<DeadLetterMessage> findAll() {
        return messagesByOrder.values().stream()
                .sorted(Comparator.comparing(DeadLetterMessage::createdAt))
                .toList();
    }

    public Optional<DeadLetterMessage> remove(UUID orderId) {
        return Optional.ofNullable(messagesByOrder.remove(orderId));
    }

    public Optional<DeadLetterMessage> findByOrderId(UUID orderId) {
        return Optional.ofNullable(messagesByOrder.get(orderId));
    }

    public int size() {
        return messagesByOrder.size();
    }
}
