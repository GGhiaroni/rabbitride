package com.gghiaroni.rabbitride.rentalservice.rental;

import com.gghiaroni.rabbitride.commons.events.AnalysisCompletedEvent;
import com.gghiaroni.rabbitride.commons.events.RentalConfirmedEvent;
import com.gghiaroni.rabbitride.commons.events.RentalFailedEvent;
import com.gghiaroni.rabbitride.commons.events.RentalRequestedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Exchanges;
import com.gghiaroni.rabbitride.commons.messaging.RoutingKeys;
import com.gghiaroni.rabbitride.rentalservice.integration.car.CarServiceClient;
import com.gghiaroni.rabbitride.rentalservice.integration.car.CarroResponse;
import com.gghiaroni.rabbitride.rentalservice.integration.car.exception.CarroIndisponivelException;
import com.gghiaroni.rabbitride.rentalservice.rental.dto.CreateRentalRequest;
import com.gghiaroni.rabbitride.rentalservice.rental.dto.RentalResponse;
import com.gghiaroni.rabbitride.rentalservice.rental.exception.RentalEmAndamentoException;
import com.gghiaroni.rabbitride.rentalservice.rental.exception.RentalNaoEncontradoException;
import com.gghiaroni.rabbitride.rentalservice.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RentalService {
    private static final Logger log = LoggerFactory.getLogger(RentalService.class);

    private static final List<StatusRental> STATUSES_ATIVOS = List.of(
        StatusRental.PENDENTE,
        StatusRental.EM_ANALISE,
        StatusRental.APROVADO
    );

    private final RentalRepository rentalRepository;

    private final RabbitTemplate rabbitTemplate;

    private final CarServiceClient carServiceClient;

    public RentalService(RentalRepository rentalRepository, RabbitTemplate rabbitTemplate, CarServiceClient carServiceClient) {
        this.rentalRepository = rentalRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.carServiceClient = carServiceClient;
    }

    @Transactional
    public RentalResponse criar(AuthenticatedUser user, CreateRentalRequest request) {
        if (rentalRepository.existsByUserIdAndStatusIn(user.id(), STATUSES_ATIVOS)) {
            throw new RentalEmAndamentoException();
        }

        Rental rental = Rental.builder()
            .userId(user.id())
            .userEmail(user.email())
            .carroId(request.carroId())
            .status(StatusRental.PENDENTE)
            .build();

        Rental salvo = rentalRepository.save(rental);
        log.info("Rental criado: id={}, userId={}, carroId={}", salvo.getId(), user.id(), request.carroId());

        RentalRequestedEvent evento = RentalRequestedEvent.of(
            salvo.getId(),
            user.id(),
            user.email(),
            request.carroId()
        );

        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL,
            RoutingKeys.RENTAL_REQUESTED,
            evento
        );

        log.info("Evento RentalRequested publicado: eventId={}, rentalId={}", evento.eventId(), salvo.getId());

        return RentalResponse.from(salvo);
    }

    @Transactional
    public void processarResultadoAnalise(AnalysisCompletedEvent event){
        Rental rental = rentalRepository.findById(event.rentalId())
            .orElseThrow(()-> new RentalNaoEncontradoException(event.rentalId()));

        if(event.resultado() == AnalysisCompletedEvent.Resultado.REJECTED){
            processarRejeicao(rental, event.motivo());
        } else {
            processarAprovacao(rental);
        }
    }

    private void processarRejeicao(Rental rental, String motivo){
        rental.marcarComoRejeitado(motivo);
        log.info("Rental {} rejeitado: {}", rental.getId(), motivo);
        publicarRentalFailed(rental, motivo);
    }

    private void processarAprovacao(Rental rental){
        try {
            CarroResponse carro = carServiceClient.reservar(rental.getCarroId());
            rental.marcarComoConfirmado();
            log.info("Rental {} confirmado: carro {} reservado", rental.getId(), carro.placa());
            publicarRentalConfirmed(rental, carro);
        } catch (CarroIndisponivelException ex) {
            String motivo = "Carro não está mais disponível para reserva.";
            rental.marcarComoFalha(motivo);
            log.warn("Rental {} falhou: {}", rental.getId(), motivo);
            publicarRentalFailed(rental, motivo);
        }
    }

    private void publicarRentalConfirmed(Rental rental, CarroResponse carro) {
        String descricao = String.format("%s %s %s (%d)",
            carro.marca(), carro.modelo(), carro.cor(), carro.ano());

        RentalConfirmedEvent evento = RentalConfirmedEvent.of(
            rental.getId(), rental.getUserEmail(), descricao);

        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL, RoutingKeys.RENTAL_CONFIRMED, evento);
    }

    private void publicarRentalFailed(Rental rental, String motivo) {
        RentalFailedEvent evento = RentalFailedEvent.of(
            rental.getId(), rental.getUserEmail(), motivo);

        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL, RoutingKeys.RENTAL_FAILED, evento);
    }
}
