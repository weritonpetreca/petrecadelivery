package com.petreca.petrecadelivery.delivery.tracking.infrastructure.event;

import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryFulfilledEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryPickedUpEvent;
import com.petreca.petrecadelivery.delivery.tracking.domain.event.DeliveryPlacedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.petreca.petrecadelivery.delivery.tracking.infrastructure.kafka.KafkaTopicConfig.DELIVERY_EVENTS_TOPIC_NAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("Delivery Domain Event Handler")
class DeliveryDomainEventHandlerTest {

    @Mock
    private IntegrationEventPublisher integrationEventPublisher;

    @InjectMocks
    private DeliveryDomainEventHandler eventHandler;

    @Test
    @DisplayName("should relay DeliveryPlacedEvent on Kafka Publisher")
    void shouldRelayPlacedEvent() {
        UUID deliveryId = UUID.randomUUID();
        DeliveryPlacedEvent event = mock(DeliveryPlacedEvent.class);
        given(event.getDeliveryId()).willReturn(deliveryId);

        eventHandler.handle(event);

        then(integrationEventPublisher).should().publish(
                eq(event),
                eq(deliveryId.toString()),
                eq(DELIVERY_EVENTS_TOPIC_NAME)
        );
    }

    @Test
    @DisplayName("should relay DeliveryPickedUpEvent on Kafka Publisher")
    void shouldRelayPickedUpEvent() {
        UUID deliveryId = UUID.randomUUID();
        DeliveryPickedUpEvent event = mock(DeliveryPickedUpEvent.class);
        given(event.getDeliveryId()).willReturn(deliveryId);

        eventHandler.handle(event);

        then(integrationEventPublisher).should().publish(
                eq(event),
                eq(deliveryId.toString()),
                eq(DELIVERY_EVENTS_TOPIC_NAME)
        );
    }

    @Test
    @DisplayName("should relay DeliveryFulfilledEvent on Kafka Publisher")
    void shouldRelayFulfilledEvent() {
        UUID deliveryID = UUID.randomUUID();
        DeliveryFulfilledEvent event = mock(DeliveryFulfilledEvent.class);
        given(event.getDeliveryId()).willReturn(deliveryID);

        eventHandler.handle(event);

        then(integrationEventPublisher).should().publish(
                eq(event),
                eq(deliveryID.toString()),
                eq(DELIVERY_EVENTS_TOPIC_NAME)
        );
    }
}
