package com.petreca.petrecadelivery.delivery.tracking.api.model;


import jakarta.validation.constraints.NotBlank;

public record ContactPointInput(
        @NotBlank String zipCode,
        @NotBlank String street,
        @NotBlank String number,
        String complement,
        @NotBlank String name,
        @NotBlank String phone
){}
