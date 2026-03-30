package com.petreca.petrecadelivery.delivery.tracking.infrastructure.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Resilience4jCircuitBreakerEventConsumer implements RegistryEventConsumer<CircuitBreaker> {

    @Override
    public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
        entryAddedEvent.getAddedEntry().getEventPublisher()
                .onEvent(event -> log.info(event.toString()));
    }

    @Override
    public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
        // Intentionally empty: No cleanup or logging is required when a circuit breaker is removed from the registry.
    }

    @Override
    public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
        // Intentionally empty: No specific logic is required when a circuit breaker is dynamically replaced.
    }
}
