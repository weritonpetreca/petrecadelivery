package com.petreca.petrecadelivery.delivery.tracking.domain.service;

import com.petreca.petrecadelivery.delivery.tracking.domain.exception.DomainException;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.ContactPoint;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.Delivery;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.DeliveryStatus;
import com.petreca.petrecadelivery.delivery.tracking.domain.repository.DeliveryRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("Delivery Checkpoint Service")
class DeliveryCheckpointServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryCheckpointService checkpointService;

    private Delivery createPreparedDelivery() {
        Delivery delivery = Delivery.draft();

        ContactPoint dummyContact = ContactPoint.builder()
                .zipCode("12345-678")
                .street("Street")
                .number("123")
                .name("Name")
                .phone("1234-5678")
                .build();

        Delivery.PreparationDetails details = Delivery.PreparationDetails.builder()
                .sender(dummyContact)
                .recipient(dummyContact)
                .distanceFee(BigDecimal.TEN)
                .courierPayout(BigDecimal.ONE)
                .expectedDeliveryTime(Duration.ofHours(1))
                .build();

        delivery.editPreparationDetails(details);
        delivery.addItem("Sword", 1);
        return delivery;
    }

    @Test
    @DisplayName("should pick up delivery, change status, and save")
    void shouldPickUpAndSave() {
        UUID deliveryId = UUID.randomUUID();
        UUID courierID = UUID.randomUUID();

        Delivery realDelivery = createPreparedDelivery();
        realDelivery.place();

        given(deliveryRepository.findById(deliveryId)).willReturn(Optional.of(realDelivery));

        checkpointService.pickUp(deliveryId, courierID);

        assertThat(realDelivery.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
        assertThat(realDelivery.getCourierId()).isEqualTo(courierID);

        then(deliveryRepository).should().saveAndFlush(realDelivery);
    }

    @Test
    @DisplayName("should complete delivery, change status, and save")
    void shouldCompleteAndSave() {
        UUID deliveryId = UUID.randomUUID();
        Delivery realDelivery = createPreparedDelivery();
        realDelivery.place();
        realDelivery.pickUp(UUID.randomUUID());

        given(deliveryRepository.findById(deliveryId)).willReturn(Optional.of(realDelivery));

        checkpointService.complete(deliveryId);

        assertThat(realDelivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        then(deliveryRepository).should().saveAndFlush(realDelivery);
    }

    @Test
    @DisplayName("should throw DomainException if delivery not found")
    void shouldThrowIfNotFound() {
        UUID deliveryId = UUID.randomUUID();
        given(deliveryRepository.findById(deliveryId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> checkpointService.place(deliveryId))
                .isInstanceOf(DomainException.class);
    }
}
