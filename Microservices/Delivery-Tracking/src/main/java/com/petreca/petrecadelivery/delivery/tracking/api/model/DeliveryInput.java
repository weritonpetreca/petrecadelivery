package com.petreca.petrecadelivery.delivery.tracking.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DeliveryInput(
    @NotNull @Valid ContactPointInput sender,
    @NotNull @Valid ContactPointInput recipient,
    @NotEmpty @Valid @Size(min = 1) List<ItemInput> items
) {}
