package com.gustavo.marketflow.execution.application;

import com.gustavo.marketflow.execution.domain.DeadLetterMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeadLetterQueueTest {

    @Test
    void addThenRemove_existingMessage_returnsAndDeletesMessage() {
        DeadLetterQueue queue = new DeadLetterQueue();
        DeadLetterMessage message = DeadLetterMessage.create(
                UUID.randomUUID(),
                "processing failed",
                3,
                Map.of("correlationId", "test-correlation")
        );
        queue.add(message);

        assertThat(queue.findByOrderId(message.orderId())).contains(message);
        assertThat(queue.remove(message.orderId())).contains(message);
        assertThat(queue.findAll()).isEmpty();
    }

    @Test
    void add_sameOrderTwice_keepsLatestTerminalFailure() {
        DeadLetterQueue queue = new DeadLetterQueue();
        UUID orderId = UUID.randomUUID();
        queue.add(DeadLetterMessage.create(orderId, "first failure", 3, Map.of()));
        DeadLetterMessage latest = DeadLetterMessage.create(orderId, "second failure", 3, Map.of());

        queue.add(latest);

        assertThat(queue.findAll()).containsExactly(latest);
    }
}
