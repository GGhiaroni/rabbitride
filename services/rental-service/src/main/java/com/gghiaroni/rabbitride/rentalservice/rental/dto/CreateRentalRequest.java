package com.gghiaroni.rabbitride.rentalservice.rental.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateRentalRequest(
    @NotNull(message = "carroId é obrigatório")
    UUID carroId
) {
}
