package com.petreca.petrecadelivery.delivery.tracking.domain.event;

import java.time.OffsetDateTime;

/**
 * The core contract for all events generated within the Delivery Domain.
 * Enforces that every event must carry the exact moment it occurred.
 */
public interface DeliveryDomainEvent {
    OffsetDateTime occurredAt();
}
