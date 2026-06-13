package com.gghiaroni.rabbitride.rentalservice.rental;

import com.gghiaroni.rabbitride.rentalservice.rental.dto.CreateRentalRequest;
import com.gghiaroni.rabbitride.rentalservice.rental.dto.RentalResponse;
import com.gghiaroni.rabbitride.rentalservice.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentals")
@Tag(name = "Rentals", description = "Gerenciamento de aluguel de veículos")
public class RentalController {
    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    @Operation(summary = "Criar novo aluguel", description = "Cria uma nova solicitação de aluguel para o usuário autenticado",
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Aluguel criado com sucesso e em processamento",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RentalResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Carro não encontrado"),
        @ApiResponse(responseCode = "409", description = "Carro não está disponível")
    })
    public ResponseEntity<RentalResponse> criar(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody CreateRentalRequest request
    ){
        return ResponseEntity.accepted().body(rentalService.criar(user, request));
    }
}
