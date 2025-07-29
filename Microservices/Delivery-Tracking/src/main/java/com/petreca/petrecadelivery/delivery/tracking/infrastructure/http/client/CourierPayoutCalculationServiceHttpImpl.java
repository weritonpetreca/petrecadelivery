package com.petreca.petrecadelivery.delivery.tracking.infrastructure.http.client;

import com.petreca.petrecadelivery.delivery.tracking.domain.service.CourierPayoutCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class CourierPayoutCalculationServiceHttpImpl implements CourierPayoutCalculationService {

    private final CourierAPIClient courierAPIClient;

    @Override
    public BigDecimal calculatePayout(Double distanceInKm) {
        CourierPayoutResultModel courierPayoutResultModel = courierAPIClient.payoutCalculation(
                new CourierPayoutCalculationInput(distanceInKm));
        return courierPayoutResultModel.getPayoutFee();
    }
}
