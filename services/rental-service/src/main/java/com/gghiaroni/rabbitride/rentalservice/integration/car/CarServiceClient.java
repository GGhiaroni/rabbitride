package com.gghiaroni.rabbitride.rentalservice.integration.car;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
    name="car-service",
    url= "${car-service.url}",
    configuration = CarServiceFeignConfig.class
)
public interface CarServiceClient {
    @PatchMapping("/internal/carros/{id}/reserve")
    CarroResponse reservar(@PathVariable UUID id);

    @PatchMapping("/internal/carros/{id}/release")
    CarroResponse liberar(@PathVariable UUID id);
}
