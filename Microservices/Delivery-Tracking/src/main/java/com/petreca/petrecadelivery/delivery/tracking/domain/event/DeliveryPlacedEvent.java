package com.petreca.petrecadelivery.delivery.tracking.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryPlacedEvent(
        OffsetDateTime occurredAt,
        UUID deliveryId
) implements DeliveryDomainEvent {

    // Custom constructor to automatically generate the timestamp if not provided
    public DeliveryPlacedEvent(UUID deliveryId) {
        this(OffsetDateTime.now(), deliveryId);
    }
}
