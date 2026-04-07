package com.petreca.petrecadelivery.courier.management.domain.service;

import com.petreca.petrecadelivery.courier.management.domain.exception.DomainException;
import com.petreca.petrecadelivery.courier.management.domain.exception.NoCouriersAvailableException;
import com.petreca.petrecadelivery.courier.management.domain.model.Courier;
import com.petreca.petrecadelivery.courier.management.domain.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourierDeliveryService {

    private final CourierRepository courierRepository;

    public void assign(UUID deliveryId, UUID courierId){
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new DomainException("Courier not found for assigment: " + courierId));
        courier.assign(deliveryId);
        courierRepository.saveAndFlush(courier);
        log.info("Courier {} assigned to delivery {}", courier.getId(), deliveryId);
    }

    public void fulfill(UUID deliveryId) {
        Courier courier = courierRepository.findByPendingDeliveries_id(deliveryId)
                .orElseThrow(() -> new DomainException("No courier found currently assigned to delivery: " + deliveryId));
        courier.fulfill(deliveryId);
        courierRepository.saveAndFlush(courier);
        log.info("Courier {} fulfilled delivery {}", courier.getId(), deliveryId);
    }
}
