package com.petreca.petrecadelivery.delivery.tracking.domain.service;

import com.petreca.petrecadelivery.delivery.tracking.api.model.ContactPointInput;
import com.petreca.petrecadelivery.delivery.tracking.api.model.DeliveryInput;
import com.petreca.petrecadelivery.delivery.tracking.api.model.ItemInput;
import com.petreca.petrecadelivery.delivery.tracking.domain.exception.DomainException;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.ContactPoint;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.Delivery;
import com.petreca.petrecadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryPreparationService {

    private final DeliveryRepository deliveryRepository;

    private final DeliveryTimeEstimationService deliveryTimeEstimationService;
    private final CourierPayoutCalculationService courierPayoutCalculationService;

    @Transactional
    public Delivery draft(DeliveryInput input) {
        Delivery delivery = Delivery.draft();
        handlePreparation(input, delivery);
        return deliveryRepository.saveAndFlush(delivery);
    }

    @Transactional
    public Delivery edit(UUID deliveryId, DeliveryInput input) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DomainException("Delivery with ID " + deliveryId + " not found."));
        delivery.removeItems();
        handlePreparation(input, delivery);
        return deliveryRepository.saveAndFlush(delivery);
    }

    private void handlePreparation(DeliveryInput input, Delivery delivery) {
        ContactPointInput senderInput = input.sender();
        ContactPointInput recipientInput = input.recipient();

        ContactPoint sender = ContactPoint.builder()
                .phone(senderInput.phone())
                .name(senderInput.name())
                .complement(senderInput.complement())
                .number(senderInput.number())
                .zipCode(senderInput.zipCode())
                .street(senderInput.street())
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .phone(recipientInput.phone())
                .name(recipientInput.name())
                .complement(recipientInput.complement())
                .number(recipientInput.number())
                .zipCode(recipientInput.zipCode())
                .street(recipientInput.street())
                .build();

        DeliveryEstimate estimate = deliveryTimeEstimationService.estimate(sender, recipient);
        BigDecimal calculatedPayout = courierPayoutCalculationService.calculatePayout(estimate.getDistanceInKm());
        
        BigDecimal distanceFee = calculateFee(estimate.getDistanceInKm());


        Delivery.PreparationDetails preparationDetails = Delivery.PreparationDetails.builder()
                .recipient(recipient)
                .sender(sender)
                .expectedDeliveryTime(estimate.getEstimatedTime())
                .courierPayout(calculatedPayout)
                .distanceFee(distanceFee)
                .build();

        delivery.editPreparationDetails(preparationDetails);
        for (ItemInput itemInput: input.items()) {
            delivery.addItem(itemInput.name(), itemInput.quantity());
        }
    }

    private BigDecimal calculateFee(Double distanceInKm) {
        return new BigDecimal("3").multiply(new BigDecimal(distanceInKm)).setScale(2, RoundingMode.HALF_EVEN);
    }


}
