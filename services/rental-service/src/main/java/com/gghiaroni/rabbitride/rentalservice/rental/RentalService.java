package com.gghiaroni.rabbitride.rentalservice.rental;

import com.gghiaroni.rabbitride.commons.events.RentalRequestedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Exchanges;
import com.gghiaroni.rabbitride.commons.messaging.RoutingKeys;
import com.gghiaroni.rabbitride.rentalservice.rental.dto.CreateRentalRequest;
import com.gghiaroni.rabbitride.rentalservice.rental.dto.RentalResponse;
import com.gghiaroni.rabbitride.rentalservice.rental.exception.RentalEmAndamentoException;
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

    public RentalService(RentalRepository rentalRepository, RabbitTemplate rabbitTemplate) {
        this.rentalRepository = rentalRepository;
        this.rabbitTemplate = rabbitTemplate;
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
}
