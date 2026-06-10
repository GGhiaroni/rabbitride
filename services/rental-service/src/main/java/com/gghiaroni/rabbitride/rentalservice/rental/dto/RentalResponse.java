package com.gghiaroni.rabbitride.rentalservice.rental.dto;

import com.gghiaroni.rabbitride.rentalservice.rental.Rental;

import java.time.Instant;
import java.util.UUID;

public record RentalResponse(
    UUID id,
    UUID carroId,
    String status,
    String statusDescricao,
    Instant criadoEm
) {
    public static RentalResponse from(Rental rental) {
        return new RentalResponse(
            rental.getId(),
            rental.getCarroId(),
            rental.getStatus().name(),
            rental.getStatus().descricao(),
            rental.getCriadoEm()
        );
    }
}
