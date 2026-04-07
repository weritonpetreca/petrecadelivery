package com.petreca.petrecadelivery.courier.management.infrastructure.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeliveryPickedUpIntegrationEvent(
        OffsetDateTime occurredAt,
        UUID deliveryId,
        UUID courierId
) {}
