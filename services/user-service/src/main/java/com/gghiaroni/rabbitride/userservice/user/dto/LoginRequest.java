package com.gghiaroni.rabbitride.userservice.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
    name = "LoginRequest",
    description = "Request para realizar login",
    example = """
        {
          "email": "usuario@example.com",
          "senha": "senha123456"
        }
        """
)
public record LoginRequest(
    @NotBlank
    @Email
    @Schema(description = "Email do usuário", example = "usuario@example.com")
    String email,

    @NotBlank
    @Schema(description = "Senha do usuário", example = "senha123456")
    String senha
) {
}
