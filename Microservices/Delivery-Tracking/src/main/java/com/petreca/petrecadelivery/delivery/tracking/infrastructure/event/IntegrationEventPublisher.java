package com.petreca.petrecadelivery.delivery.tracking.infrastructure.event;

public interface IntegrationEventPublisher {
    void publish(Object event, String key, String topic);
}
