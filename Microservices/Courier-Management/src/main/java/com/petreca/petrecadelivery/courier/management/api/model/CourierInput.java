package com.petreca.petrecadelivery.courier.management.api.model;

import jakarta.validation.constraints.NotBlank;

public record CourierInput(
        @NotBlank String name,
        @NotBlank String phone
) {}
