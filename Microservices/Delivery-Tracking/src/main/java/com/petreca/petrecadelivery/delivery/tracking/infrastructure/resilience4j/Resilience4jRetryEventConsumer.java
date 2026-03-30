package com.petreca.petrecadelivery.delivery.tracking.infrastructure.resilience4j;

import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Resilience4jRetryEventConsumer implements RegistryEventConsumer<Retry> {
    @Override
    public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
        entryAddedEvent.getAddedEntry().getEventPublisher()
                .onEvent(event -> log.info(event.toString()));
    }

    @Override
    public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {
        // Intentionally empty: No cleanup or logging is required when a circuit breaker is removed from the registry.
    }

    @Override
    public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {
        // Intentionally empty: No specific logic is required when a circuit breaker is dynamically replaced.
    }
}
