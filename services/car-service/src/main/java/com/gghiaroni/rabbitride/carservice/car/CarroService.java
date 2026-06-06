package com.gghiaroni.rabbitride.carservice.car;

import com.gghiaroni.rabbitride.carservice.car.dto.CarroResponse;
import com.gghiaroni.rabbitride.carservice.car.dto.CreateCarroRequest;
import com.gghiaroni.rabbitride.carservice.car.dto.PagedResult;
import com.gghiaroni.rabbitride.carservice.car.dto.UpdateCarroRequest;
import com.gghiaroni.rabbitride.carservice.car.exception.CarroNotAvailableException;
import com.gghiaroni.rabbitride.carservice.car.exception.CarroNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CarroService {
    private final CarroRepository carroRepository;

    public CarroService(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }

    @Cacheable(value = "cars-list", key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagedResult<CarroResponse> listar(StatusCarro status, Pageable pageable){
        Page<Carro> page = (status != null)
            ? carroRepository.findByStatus(status, pageable)
            : carroRepository.findAll(pageable);

        return PagedResult.from(page.map(CarroResponse::from));
    }

    @Cacheable(value = "cars-by-id", key = "#id")
    @Transactional(readOnly = true)
    public CarroResponse buscarPorId(UUID id){
        Carro carro = carroRepository.findById(id)
            .orElseThrow(() -> new CarroNotFoundException(id));
        return CarroResponse.from(carro);
    }

    @CacheEvict(value = "cars-list", allEntries = true)
    @Transactional
    public CarroResponse criar(CreateCarroRequest request){
        Carro carro = Carro.builder()
            .placa(request.placa())
            .ano(request.ano())
            .modelo(request.modelo())
            .marca(request.marca())
            .cor(request.cor())
            .valorDiaria(request.valorDiaria())
            .status(StatusCarro.DISPONIVEL)
            .build();

        return CarroResponse.from(carroRepository.save(carro));
    }

    @Caching(evict = {
        @CacheEvict(value = "cars-by-id", key = "#id"),
        @CacheEvict(value = "cars-list", allEntries = true)
    })
    @Transactional
    public CarroResponse atualizar(UUID id, UpdateCarroRequest request){
        Carro existente = buscarEntidadePorId(id);

        Carro carro = Carro.builder()
            .id(existente.getId())
            .placa(request.placa())
            .ano(request.ano())
            .modelo(request.modelo())
            .marca(request.marca())
            .cor(request.cor())
            .valorDiaria(request.valorDiaria())
            .status(existente.getStatus())
            .versao(existente.getVersao())
            .criadoEm(existente.getCriadoEm())
            .build();

        return CarroResponse.from(carroRepository.save(carro));
    }

    @Caching(evict = {
        @CacheEvict(value = "cars-by-id", key = "#id"),
        @CacheEvict(value = "cars-list", allEntries = true)
    })
    @Transactional
    public void deletar(UUID id){
        Carro carro = buscarEntidadePorId(id);
        carroRepository.delete(carro);
    }

    @Caching(evict = {
        @CacheEvict(value = "cars-by-id", key = "#id"),
        @CacheEvict(value = "cars-list", allEntries = true)
    })
    @Transactional
    public CarroResponse reservar(UUID id){
        Carro carro = buscarEntidadePorId(id);

        if (carro.getStatus() != StatusCarro.DISPONIVEL) {
            throw new CarroNotAvailableException(id, carro.getStatus(), StatusCarro.DISPONIVEL);
        }

        Carro reservado = rebuildComStatus(carro, StatusCarro.RESERVADO);

        return CarroResponse.from(carroRepository.save(reservado));
    }

    @Caching(evict = {
        @CacheEvict(value = "cars-by-id", key = "#id"),
        @CacheEvict(value = "cars-list", allEntries = true)
    })
    @Transactional
    public CarroResponse liberar(UUID id) {
        Carro carro = buscarEntidadePorId(id);

        if (carro.getStatus() != StatusCarro.RESERVADO) {
            throw new CarroNotAvailableException(id, carro.getStatus(), StatusCarro.RESERVADO);
        }

        Carro liberado = rebuildComStatus(carro, StatusCarro.DISPONIVEL);
        return CarroResponse.from(carroRepository.save(liberado));
    }

    private Carro buscarEntidadePorId(UUID id){
        return carroRepository.findById(id)
            .orElseThrow(() -> new CarroNotFoundException(id));
    }

    private Carro rebuildComStatus(Carro original, StatusCarro novoStatus){
       return Carro.builder()
           .id(original.getId())
           .placa(original.getPlaca())
           .modelo(original.getModelo())
           .marca(original.getMarca())
           .cor(original.getCor())
           .ano(original.getAno())
           .valorDiaria(original.getValorDiaria())
           .status(novoStatus)
           .versao(original.getVersao())
           .criadoEm(original.getCriadoEm())
           .build();
    }
}
