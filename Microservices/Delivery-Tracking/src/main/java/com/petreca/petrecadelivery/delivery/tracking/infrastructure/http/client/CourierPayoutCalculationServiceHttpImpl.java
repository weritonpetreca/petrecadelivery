package com.petreca.petrecadelivery.delivery.tracking.infrastructure.http.client;

import com.petreca.petrecadelivery.delivery.tracking.domain.service.CourierPayoutCalculationService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class CourierPayoutCalculationServiceHttpImpl implements CourierPayoutCalculationService {

    private final CourierAPIClient courierAPIClient;

    @Override
    public BigDecimal calculatePayout(Double distanceInKm) {
        try {
            CourierPayoutResultModel courierPayoutResultModel1 = courierAPIClient.payoutCalculation(
                    new CourierPayoutCalculationInput(distanceInKm));
            return courierPayoutResultModel1.getPayoutFee();
        } catch (ResourceAccessException e) {
            throw new GatewayTimeoutException(e);
        } catch (HttpServerErrorException | CallNotPermittedException | IllegalArgumentException e) {
            throw new BadGatewayException(e);
        }
    }
}
