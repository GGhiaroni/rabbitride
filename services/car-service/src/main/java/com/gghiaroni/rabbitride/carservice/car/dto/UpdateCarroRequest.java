package com.gghiaroni.rabbitride.carservice.car.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateCarroRequest(
    @NotBlank @Size(min = 7, max = 7) String placa,
    @NotBlank @Size(max = 50) String modelo,
    @NotBlank @Size(max = 50) String marca,
    @NotBlank @Size(max = 30) String cor,
    @NotNull @Min(1990) @Max(2030) Integer ano,
    @NotNull @DecimalMin("10.00") BigDecimal valorDiaria
) {
}
