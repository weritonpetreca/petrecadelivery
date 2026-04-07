package com.petreca.petrecadelivery.delivery.tracking.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryPickedUpEvent(
        OffsetDateTime occurredAt,
        UUID deliveryId,
        UUID courierId) implements DeliveryDomainEvent {

    public DeliveryPickedUpEvent(UUID deliveryId, UUID courierId) {
        this(OffsetDateTime.now(), deliveryId, courierId);
    }
}
