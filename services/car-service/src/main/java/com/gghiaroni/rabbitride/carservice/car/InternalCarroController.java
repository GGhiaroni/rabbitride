package com.gghiaroni.rabbitride.carservice.car;

import com.gghiaroni.rabbitride.carservice.car.dto.CarroResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/carros")
public class InternalCarroController {
    private final CarroService carroService;

    public InternalCarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    @PatchMapping("/{id}/reserve")
    public ResponseEntity<CarroResponse> reservar(@PathVariable UUID id) {
        return ResponseEntity.ok(carroService.reservar(id));
    }

    @PatchMapping("/{id}/release")
    public ResponseEntity<CarroResponse> liberar(@PathVariable UUID id) {
        return ResponseEntity.ok(carroService.liberar(id));
    }
}
