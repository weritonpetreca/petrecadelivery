package com.petreca.petrecadelivery.delivery.tracking.domain.repository;

import com.petreca.petrecadelivery.delivery.tracking.domain.model.ContactPoint;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.Delivery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeliveryRepositoryTest {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Test
    public void shouldPersist() {
        Delivery delivery = Delivery.draft();

        delivery.editPreparationDetails(createValidPreparationDetails());

        delivery.addItem("Computador",2);
        delivery.addItem("Notebook", 3);

        deliveryRepository.saveAndFlush(delivery);

        Delivery persistedDelivery = deliveryRepository.findById(delivery.getId()).orElseThrow();

        assertEquals(2, persistedDelivery.getItems().size());
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