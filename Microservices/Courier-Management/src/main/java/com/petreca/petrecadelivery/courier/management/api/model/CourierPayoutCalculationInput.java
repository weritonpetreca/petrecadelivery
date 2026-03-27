package com.petreca.petrecadelivery.courier.management.api.model;

import lombok.Getter;
import lombok.Setter;

public record CourierPayoutCalculationInput(
        Double distanceInKm
) {}
