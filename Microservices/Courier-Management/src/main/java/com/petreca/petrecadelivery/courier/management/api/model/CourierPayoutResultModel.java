package com.petreca.petrecadelivery.courier.management.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public record CourierPayoutResultModel(
    BigDecimal payoutFee
){}
