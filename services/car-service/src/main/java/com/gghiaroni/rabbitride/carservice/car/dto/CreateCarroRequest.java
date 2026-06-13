package com.gghiaroni.rabbitride.carservice.car.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(
    name = "CreateCarroRequest",
    description = "Request para criar um novo carro",
    example = """
        {
          "placa": "ABC1234",
          "modelo": "Civic",
          "marca": "Honda",
          "cor": "Preto",
          "ano": 2023,
          "valorDiaria": 150.00
        }
        """
)
public record CreateCarroRequest(
    @NotBlank(message = "Placa é obrigatória")
    @Size(min = 7, max = 7, message = "Placa deve conter exatamente 7 caracteres")
    @Schema(description = "Placa do veículo em formato brasileiro", example = "ABC1234")
    String placa,

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 50)
    @Schema(description = "Modelo do veículo", example = "Civic")
    String modelo,

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50)
    @Schema(description = "Marca do veículo", example = "Honda")
    String marca,

    @NotBlank(message = "Cor é obrigatória")
    @Size(max = 30)
    @Schema(description = "Cor do veículo", example = "Preto")
    String cor,

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1990, message = "Ano deve ser >= 1990")
    @Max(value = 2030, message = "Ano deve ser <= 2030")
    @Schema(description = "Ano de fabricação do veículo", example = "2023")
    Integer ano,

    @NotNull(message = "Valor da diária é obrigatório")
    @DecimalMin(value = "10.00", message = "Valor da diária deve ser pelo menos 10.00")
    @Schema(description = "Valor da diária em reais", example = "150.00")
    BigDecimal valorDiaria
) {
    public CreateCarroRequest {
        if (placa != null) placa = placa.toUpperCase().trim();
    }
}
