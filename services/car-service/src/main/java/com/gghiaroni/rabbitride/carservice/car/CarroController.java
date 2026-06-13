package com.gghiaroni.rabbitride.carservice.car;

import com.gghiaroni.rabbitride.carservice.car.dto.CarroResponse;
import com.gghiaroni.rabbitride.carservice.car.dto.CreateCarroRequest;
import com.gghiaroni.rabbitride.carservice.car.dto.PagedResult;
import com.gghiaroni.rabbitride.carservice.car.dto.UpdateCarroRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/carros")
@Tag(name = "Carros", description = "Gerenciamento de veículos disponíveis para aluguel")
public class CarroController {
    private final CarroService carroService;

    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    @GetMapping
    @Operation(summary = "Listar carros", description = "Lista todos os carros com paginação e filtro opcional por status")
    @Parameters({
        @Parameter(name = "status", description = "Filtro por status do carro (DISPONIVEL, ALUGADO, MANUTENCAO)", required = false),
        @Parameter(name = "page", description = "Número da página (padrão: 0)", required = false),
        @Parameter(name = "size", description = "Tamanho da página (padrão: 20)", required = false)
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de carros retornada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResult.class)))
    })
    public ResponseEntity<PagedResult<CarroResponse>> listar(
        @RequestParam(required = false) StatusCarro status,
        @PageableDefault(size = 20) Pageable pageable
    ){
        return ResponseEntity.ok(carroService.listar(status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar carro por ID", description = "Retorna os detalhes de um carro específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carro encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarroResponse.class))),
        @ApiResponse(responseCode = "404", description = "Carro não encontrado")
    })
    public ResponseEntity<CarroResponse> buscarPorId(
        @Parameter(description = "ID do carro", required = true)
        @PathVariable UUID id
    ){
        return ResponseEntity.ok(carroService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo carro", description = "Adiciona um novo carro ao sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Carro criado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarroResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Placa já existe no sistema")
    })
    public ResponseEntity<CarroResponse> criar(@Valid @RequestBody CreateCarroRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(carroService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar carro", description = "Atualiza os dados de um carro existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carro atualizado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CarroResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Carro não encontrado")
    })
    public ResponseEntity<CarroResponse> atualizar(
        @Parameter(description = "ID do carro", required = true)
        @PathVariable UUID id,
        @Valid @RequestBody UpdateCarroRequest request
    ){
        return ResponseEntity.ok(carroService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar carro", description = "Remove um carro do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Carro deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Carro não encontrado")
    })
    public ResponseEntity<Void> deletar(
        @Parameter(description = "ID do carro", required = true)
        @PathVariable UUID id
    ){
        carroService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
