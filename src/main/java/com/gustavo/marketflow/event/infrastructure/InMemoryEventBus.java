package com.gustavo.marketflow.event.infrastructure;

import com.gustavo.marketflow.event.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Process-local event bus used to demonstrate publish-subscribe semantics before an external broker is introduced.
 */
@Component
public class InMemoryEventBus {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventBus.class);

    private final CopyOnWriteArrayList<DomainEvent> publishedEvents;
    private final CopyOnWriteArrayList<Consumer<DomainEvent>> subscribers;

    public InMemoryEventBus() {
        this.publishedEvents = new CopyOnWriteArrayList<>();
        this.subscribers = new CopyOnWriteArrayList<>();
    }

    public void publish(DomainEvent event) {
        DomainEvent nonNullEvent = Objects.requireNonNull(event, "event");
        publishedEvents.add(nonNullEvent);
        subscribers.forEach(subscriber -> notifySubscriber(subscriber, nonNullEvent));
        log.info("Domain event published type={} eventId={} orderId={}",
                event.type(), event.eventId(), event.orderId());
    }

    public void subscribe(Consumer<DomainEvent> subscriber) {
        subscribers.add(Objects.requireNonNull(subscriber, "subscriber"));
    }

    public List<DomainEvent> findAll() {
        return List.copyOf(publishedEvents);
    }

    public List<DomainEvent> findByType(String type) {
        String normalizedType = Objects.requireNonNull(type, "type").toUpperCase(Locale.ROOT);
        return publishedEvents.stream()
                .filter(event -> event.type().equals(normalizedType))
                .toList();
    }

    private void notifySubscriber(Consumer<DomainEvent> subscriber, DomainEvent event) {
        try {
            subscriber.accept(event);
        } catch (RuntimeException ex) {
            log.error("Domain event subscriber failed type={} eventId={}", event.type(), event.eventId(), ex);
        }
    }
}
