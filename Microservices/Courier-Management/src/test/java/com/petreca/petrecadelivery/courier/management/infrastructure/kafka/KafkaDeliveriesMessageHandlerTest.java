package com.petreca.petrecadelivery.courier.management.infrastructure.kafka;

import com.petreca.petrecadelivery.courier.management.domain.service.CourierDeliveryService;
import com.petreca.petrecadelivery.courier.management.infrastructure.event.DeliveryFulfilledIntegrationEvent;
import com.petreca.petrecadelivery.courier.management.infrastructure.event.DeliveryPickedUpIntegrationEvent;
import com.petreca.petrecadelivery.courier.management.infrastructure.event.DeliveryPlacedIntegrationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka Deliveries Message Handler Test")
class KafkaDeliveriesMessageHandlerTest {

    @Mock
    private CourierDeliveryService courierDeliveryService;

    @InjectMocks
    private KafkaDeliveriesMessageHandler messageHandler;

    @Test
    @DisplayName("should NOT assign on PlacedEvent (Notice Board logic)")
    void shouldHandlePlacedEvent() {
        UUID deliveryId = UUID.randomUUID();
        DeliveryPlacedIntegrationEvent event = new DeliveryPlacedIntegrationEvent(OffsetDateTime.now(), deliveryId);

        messageHandler.handle(event);

        verifyNoInteractions(courierDeliveryService);
    }

    @Test
    @DisplayName("should route DeliveryPickedUpIntegrationEvent to explicit assign method")
    void shouldHandlePickedUpEvent() {
        UUID deliveryId = UUID.randomUUID();
        UUID courierId = UUID.randomUUID();

        DeliveryPickedUpIntegrationEvent event = new DeliveryPickedUpIntegrationEvent(
                OffsetDateTime.now(), deliveryId, courierId
        );

        messageHandler.handle(event);

        then(courierDeliveryService).should().assign(deliveryId, courierId);
    }

    @Test
    @DisplayName("should route DeliveryFulfilledIntegrationEvent to fulfill method")
    void shouldHandleFulfilledEvent() {
        UUID deliveryId = UUID.randomUUID();
        DeliveryFulfilledIntegrationEvent event = new DeliveryFulfilledIntegrationEvent(
                OffsetDateTime.now(), deliveryId
        );

        messageHandler.handle(event);

        then(courierDeliveryService).should().fulfill(deliveryId);
    }

    @Test
    @DisplayName("should log unknown objects gracefully without crashing")
    void shouldHandleDefaultObjects() {
        Object unknowEvent = new Object();

        assertDoesNotThrow(() -> messageHandler.defaultHandler(unknowEvent));
    }
}
