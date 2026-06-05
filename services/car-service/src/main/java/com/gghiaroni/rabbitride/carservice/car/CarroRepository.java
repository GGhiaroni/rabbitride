package com.gghiaroni.rabbitride.carservice.car;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarroRepository extends JpaRepository<Carro, UUID> {
    List<Carro> findByStatus(StatusCarro status);
    Optional<Carro> findByPlaca(String placa);
}
