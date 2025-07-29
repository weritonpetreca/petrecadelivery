package com.petreca.petrecadelivery.delivery.tracking.domain.service;

import com.petreca.petrecadelivery.delivery.tracking.domain.model.ContactPoint;

public interface DeliveryTimeEstimationService {

    DeliveryEstimate estimate(ContactPoint sender, ContactPoint receiver);
}
