package com.petreca.petrecadelivery.delivery.tracking.domain.model;

import com.petreca.petrecadelivery.delivery.tracking.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryTest {

    @Test
    public void shouldChangeToPlaced() {
        Delivery delivery = Delivery.draft();
        
        delivery.editPreparationDetails(createValidPreparationDetails());
        
        delivery.place();

        assertEquals(DeliveryStatus.WAITING_FOR_COURIER, delivery.getStatus());
        assertNotNull(delivery.getPlacedAt());
    }

    @Test
    public void shouldNotPlaced() {
        Delivery delivery = Delivery.draft();


        assertThrows(DomainException.class, () -> {
            delivery.place();
        });

        assertEquals(DeliveryStatus.DRAFT, delivery.getStatus());
        assertNull(delivery.getPlacedAt());
    }

    private Delivery.PreparationDetails createValidPreparationDetails() {
        ContactPoint sender = ContactPoint.builder()
                .zipCode("12345-678")
                .street("Sender Street")
                .number("123")
                .complement("Apt 1")
                .name("João Silva")
                .phone("(12) 93456-7890")
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode("87654-321")
                .street("Recipient Street")
                .number("456")
                .complement("Apt 2")
                .name("Maria Oliveira")
                .phone("(98) 98765-4321")
                .build();

        return Delivery.PreparationDetails.builder()
                .sender(sender)
                .recipient(recipient)
                .distanceFee(new BigDecimal("15.00"))
                .courierPayout(new BigDecimal(5.00))
                .expectedDeliveryTime(Duration.ofHours(5))
                .build();
    }

}