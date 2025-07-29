package com.petreca.petrecadelivery.delivery.tracking.infrastructure.fake;

import com.petreca.petrecadelivery.delivery.tracking.domain.model.ContactPoint;
import com.petreca.petrecadelivery.delivery.tracking.domain.service.DeliveryEstimate;
import com.petreca.petrecadelivery.delivery.tracking.domain.service.DeliveryTimeEstimationService;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DeliveryTimeEstimationServiceFakeImpl implements DeliveryTimeEstimationService {
    @Override
    public DeliveryEstimate estimate(ContactPoint sender, ContactPoint receiver) {
        return new DeliveryEstimate(Duration.ofHours(3), 3.1);
    }
}
