package com.gghiaroni.rabbitride.userservice.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

@Schema(
    name = "RegisterRequest",
    description = "Request para registrar um novo usuário",
    example = """
        {
          "nome": "João Silva",
          "email": "joao@example.com",
          "senha": "senha123456",
          "cpf": "123.456.789-00"
        }
        """
)
public record RegisterRequest(
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 150, message = "Nome deve conter entre 2 e 150 caracteres")
    @Schema(description = "Nome completo do usuário", example = "João Silva")
    String nome,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Schema(description = "Email do usuário", example = "joao@example.com")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 100, message = "Senha deve conter entre 8 e 100 caracteres")
    @Schema(description = "Senha do usuário (mínimo 8 caracteres)", example = "senha123456")
    String senha,

    @NotBlank(message = "CPF é obrigatório")
    @CPF(message = "CPF inválido")
    @Schema(description = "CPF do usuário no formato XXX.XXX.XXX-XX", example = "123.456.789-00")
    String cpf
) {}
