package com.petreca.petrecadelivery.courier.management.api.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public record CourierInput(
        @NotBlank String name,
        @NotBlank String phone
) {}
