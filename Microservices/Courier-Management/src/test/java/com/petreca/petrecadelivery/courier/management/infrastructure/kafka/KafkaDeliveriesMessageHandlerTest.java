package com.petreca.petrecadelivery.courier.management.infrastructure.kafka;

import com.petreca.petrecadelivery.courier.management.domain.service.CourierDeliveryService;
import com.petreca.petrecadelivery.courier.management.infrastructure.event.DeliveryFulfilledIntegrationEvent;
import com.petreca.petrecadelivery.courier.management.infrastructure.event.DeliveryPlacedIntegrationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka Deliveries Message Handler Test")
class KafkaDeliveriesMessageHandlerTest {

    @Mock
    private CourierDeliveryService courierDeliveryService;

    @InjectMocks
    private KafkaDeliveriesMessageHandler messageHandler;

    @Test
    @DisplayName("should route DeliveryPlacedIntegrationEvent to assign method")
    void shouldHandlePlacedEvent() {
        UUID deliveryId = UUID.randomUUID();
        DeliveryPlacedIntegrationEvent event = mock(DeliveryPlacedIntegrationEvent.class);
        when(event.getDeliveryId()).thenReturn(deliveryId);

        messageHandler.handle(event);

        then(courierDeliveryService).should().assign(deliveryId);
    }

    @Test
    @DisplayName("should route DeliveryFulfilledIntegrationEvent to fulfill method")
    void shouldHandleFulfilledEvent() {
        UUID deliveryId = UUID.randomUUID();
        DeliveryFulfilledIntegrationEvent event = mock(DeliveryFulfilledIntegrationEvent.class);
        when(event.getDeliveryId()).thenReturn(deliveryId);

        messageHandler.handle(event);

        then(courierDeliveryService).should().fulfill(deliveryId);
    }

    @Test
    @DisplayName("should log unkown objects gracefully without crashing")
    void shouldHandleDefaultObjects() {
        Object unknowEvent = new Object();

        assertDoesNotThrow(() -> messageHandler.defaultHandler(unknowEvent));
    }
}
