package com.gghiaroni.rabbitride.carservice.car.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateCarroRequest(
    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 7, message = "Placa deve conter exatamente 7 caracteres")
    String placa,

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 50)
    String modelo,

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50)
    String marca,

    @NotBlank(message = "Cor é obrigatória")
    @Size(max = 30)
    String cor,

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1990, message = "Ano deve ser >= 1990")
    @Max(value = 2030, message = "Ano deve ser <= 2030")
    Integer ano,

    @NotNull(message = "Valor da diária é obrigatório")
    @DecimalMin(value = "10.00", message = "Valor da diária deve ser pelo menos 10.00")
    BigDecimal valorDiaria
) {
    public CreateCarroRequest {
        if (placa != null) placa = placa.toUpperCase().trim();
    }
}
