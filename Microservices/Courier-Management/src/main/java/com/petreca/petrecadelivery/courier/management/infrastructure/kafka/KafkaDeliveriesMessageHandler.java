package com.petreca.petrecadelivery.courier.management.infrastructure.kafka;

import com.petreca.petrecadelivery.courier.management.domain.service.CourierDeliveryService;
import com.petreca.petrecadelivery.courier.management.infrastructure.event.DeliveryFulfilledIntegrationEvent;
import com.petreca.petrecadelivery.courier.management.infrastructure.event.DeliveryPickedUpIntegrationEvent;
import com.petreca.petrecadelivery.courier.management.infrastructure.event.DeliveryPlacedIntegrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = {"deliveries.v1.events"}, groupId = "courier-management")
@Slf4j
@RequiredArgsConstructor
public class KafkaDeliveriesMessageHandler {

    private final CourierDeliveryService courierDeliveryService;

    @KafkaHandler(isDefault = true)
    public void defaultHandler(@Payload Object object) {
        log.warn("Default Handler: {}", object);
    }

    @KafkaHandler
    public void handle(@Payload DeliveryPlacedIntegrationEvent event) {
        log.info("Delivery placed on Notice Board. Waiting for a Courier: {}", event);
    }

    @KafkaHandler
    public void handle(@Payload DeliveryPickedUpIntegrationEvent event) {
        log.info("Delivery Picked Up. Executing assigment: {}", event);
        courierDeliveryService.assign(event.deliveryId(), event.courierId());
    }

    @KafkaHandler
    public void handle(@Payload DeliveryFulfilledIntegrationEvent event) {
        log.info("Received: {}", event);
        courierDeliveryService.fulfill(event.deliveryId());
    }
}
