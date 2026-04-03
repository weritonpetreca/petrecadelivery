package com.petreca.petrecadelivery.delivery.tracking.api.controller;

import com.petreca.petrecadelivery.delivery.tracking.api.doc.DeliveryControllerDoc;
import com.petreca.petrecadelivery.delivery.tracking.api.model.CourierIdInput;
import com.petreca.petrecadelivery.delivery.tracking.api.model.DeliveryInput;
import com.petreca.petrecadelivery.delivery.tracking.domain.model.Delivery;
import com.petreca.petrecadelivery.delivery.tracking.domain.repository.DeliveryRepository;
import com.petreca.petrecadelivery.delivery.tracking.domain.service.DeliveryCheckpointService;
import com.petreca.petrecadelivery.delivery.tracking.domain.service.DeliveryPreparationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryControllerDoc {

    private final DeliveryPreparationService deliveryPreparationService;
    private final DeliveryCheckpointService deliveryCheckpointService;
    private final DeliveryRepository deliveryRepository;

    @Override
    public ResponseEntity<Void> draft(DeliveryInput input) {
        log.info("Drafting new delivery");
        Delivery delivery = deliveryPreparationService.draft(input);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(delivery.getId())
                .toUri();

        log.info("Delivery drafted successfully. Resource Location: {}", location);
        return ResponseEntity.created(location).build();
    }

    @Override
    public ResponseEntity<Delivery> edit(UUID deliveryId, DeliveryInput input) {
        log.info("Editing delivery with ID: {}", deliveryId);
        Delivery updatedDelivery = deliveryPreparationService.edit(deliveryId, input);
        return ResponseEntity.ok(updatedDelivery);
    }

    @SneakyThrows
    @Override
    public ResponseEntity<PagedModel<Delivery>> findAll(Pageable pageable) {
        log.info("FindAll deliveries requested");
        PagedModel<Delivery> deliveryPage = new PagedModel<>(deliveryRepository.findAll(pageable));
        return ResponseEntity.ok(deliveryPage);
    }

    @Override
    public ResponseEntity<Delivery> findById(UUID deliveryId) {
        log.info("Fetching delivery by ID: {}", deliveryId);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok(delivery);
    }

    @Override
    public ResponseEntity<Void> place(UUID deliveryId) {
        log.info("Executing placement for delivery ID: {}", deliveryId);
        deliveryCheckpointService.place(deliveryId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> pickUp(UUID deliveryId, CourierIdInput input) {
        log.info("Executing pickup for delivery ID: {} by courier ID: {}", deliveryId, input.courierId());
        deliveryCheckpointService.pickUp(deliveryId, input.courierId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> complete(UUID deliveryId) {
        log.info("Executing completion for delivery ID: {}", deliveryId);
        deliveryCheckpointService.complete(deliveryId);
        return ResponseEntity.noContent().build();
    }
}
