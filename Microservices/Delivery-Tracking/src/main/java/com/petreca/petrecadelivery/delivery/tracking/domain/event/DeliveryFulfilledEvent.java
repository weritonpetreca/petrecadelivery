package com.petreca.petrecadelivery.delivery.tracking.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryFulfilledEvent(
        OffsetDateTime occurredAt,
        UUID deliveryId
) implements DeliveryDomainEvent {

    public DeliveryFulfilledEvent(UUID deliveryId) {
        this(OffsetDateTime.now(), deliveryId);
    }
}
