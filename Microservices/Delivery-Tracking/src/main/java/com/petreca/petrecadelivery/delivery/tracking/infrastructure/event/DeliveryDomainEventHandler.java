package com.petreca.petrecadelivery.delivery.tracking.infrastructure.event;

import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryFulfilledEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryPickedUpEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeliveryDomainEventHandler {

    @EventListener
    public void handle(DeliveryPlacedEvent event) {
        log.info(event.toString());
    }

    @EventListener
    public void handle(DeliveryPickedUpEvent event) {
        log.info(event.toString());
    }

    @EventListener
    public void handle(DeliveryFulfilledEvent event) {
        log.info(event.toString());
    }
}
