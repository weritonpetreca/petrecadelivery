package com.petreca.petrecadelivery.delivery.tracking.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
public class DeliveryPickedUpEvent {

    private final OffsetDateTime occurredAt;
    private final UUID deliveryId;
}
