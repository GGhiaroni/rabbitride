package com.gghiaroni.rabbitride.rentalservice.rental.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(
    name = "CreateRentalRequest",
    description = "Request para criar um novo aluguel",
    example = """
        {
          "carroId": "550e8400-e29b-41d4-a716-446655440000"
        }
        """
)
public record CreateRentalRequest(
    @NotNull(message = "carroId é obrigatório")
    @Schema(description = "ID UUID do carro a ser alugado", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID carroId
) {
}
