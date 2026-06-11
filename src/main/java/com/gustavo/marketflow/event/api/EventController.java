package com.gustavo.marketflow.event.api;

import com.gustavo.marketflow.event.domain.DomainEvent;
import com.gustavo.marketflow.event.infrastructure.InMemoryEventBus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only REST adapter for inspecting events published by the internal bus.
 */
@RestController
@RequestMapping("/events")
public class EventController {

    private final InMemoryEventBus eventBus;

    public EventController(InMemoryEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @GetMapping
    public List<DomainEvent> findAll() {
        return eventBus.findAll();
    }

    @GetMapping("/{type}")
    public List<DomainEvent> findByType(@PathVariable String type) {
        return eventBus.findByType(type);
    }
}
