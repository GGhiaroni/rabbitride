package com.gghiaroni.rabbitride.carservice.car.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(
    name = "UpdateCarroRequest",
    description = "Request para atualizar dados de um carro"
)
public record UpdateCarroRequest(
    @NotBlank @Size(min = 7, max = 7)
    @Schema(description = "Placa do veículo em formato brasileiro", example = "ABC1234")
    String placa,

    @NotBlank @Size(max = 50)
    @Schema(description = "Modelo do veículo", example = "Civic")
    String modelo,

    @NotBlank @Size(max = 50)
    @Schema(description = "Marca do veículo", example = "Honda")
    String marca,

    @NotBlank @Size(max = 30)
    @Schema(description = "Cor do veículo", example = "Preto")
    String cor,

    @NotNull @Min(1990) @Max(2030)
    @Schema(description = "Ano de fabricação do veículo", example = "2023")
    Integer ano,

    @NotNull @DecimalMin("10.00")
    @Schema(description = "Valor da diária em reais", example = "150.00")
    BigDecimal valorDiaria
) {
}
