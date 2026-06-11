package com.gustavo.marketflow.event.infrastructure;

import com.gustavo.marketflow.event.domain.OrderCreatedEvent;
import com.gustavo.marketflow.event.domain.OrderQueuedEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryEventBusTest {

    @Test
    void publish_withSubscriber_storesAndDeliversEvent() {
        InMemoryEventBus eventBus = new InMemoryEventBus();
        AtomicReference<String> observedType = new AtomicReference<>();
        eventBus.subscribe(event -> observedType.set(event.type()));
        OrderCreatedEvent event = OrderCreatedEvent.now(UUID.randomUUID());

        eventBus.publish(event);

        assertThat(eventBus.findAll()).containsExactly(event);
        assertThat(observedType.get()).isEqualTo("ORDER_CREATED");
    }

    @Test
    void findByType_withMixedEvents_returnsOnlyRequestedType() {
        InMemoryEventBus eventBus = new InMemoryEventBus();
        UUID orderId = UUID.randomUUID();
        eventBus.publish(OrderCreatedEvent.now(orderId));
        OrderQueuedEvent queuedEvent = OrderQueuedEvent.now(orderId);
        eventBus.publish(queuedEvent);

        assertThat(eventBus.findByType("order_queued")).containsExactly(queuedEvent);
    }
}
