package com.petreca.petrecadelivery.delivery.tracking.api.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemInput(
        @NotBlank String name,
        @NotNull @Min(1) Integer quantity
) {}
