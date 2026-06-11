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
import com.gghiaroni.rabbitride.rentalservice.messaging.ProcessedEvent;
import com.gghiaroni.rabbitride.rentalservice.messaging.ProcessedEventRepository;
import com.gghiaroni.rabbitride.rentalservice.rental.dto.CreateRentalRequest;
import com.gghiaroni.rabbitride.rentalservice.rental.dto.RentalResponse;
import com.gghiaroni.rabbitride.rentalservice.rental.exception.RentalEmAndamentoException;
import com.gghiaroni.rabbitride.rentalservice.rental.exception.RentalNaoEncontradoException;
import com.gghiaroni.rabbitride.rentalservice.security.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock private RentalRepository rentalRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private CarServiceClient carServiceClient;
    @Mock private ProcessedEventRepository processedEventRepository;

    @InjectMocks private RentalService rentalService;

    private final AuthenticatedUser user = new AuthenticatedUser(
        UUID.randomUUID(), "gabriel@test.com", "Gabriel");

    @Test
    @DisplayName("criar: salva rental como PENDENTE e publica RentalRequested")
    void criarDeveSalvarPendenteEPublicarEvento() {
        CreateRentalRequest request = new CreateRentalRequest(UUID.randomUUID());
        when(rentalRepository.existsByUserIdAndStatusIn(any(), any())).thenReturn(false);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(this::devolverComoSeFosseSalvo);

        RentalResponse response = rentalService.criar(user, request);

        assertThat(response.status()).isEqualTo("PENDENTE");
        verify(rabbitTemplate).convertAndSend(
            eq(Exchanges.RENTAL),
            eq(RoutingKeys.RENTAL_REQUESTED),
            any(RentalRequestedEvent.class)
        );
    }

    @Test
    @DisplayName("criar: lança exception se usuário já tem rental ativo")
    void criarDeveLancarExceptionSeUsuarioTemRentalAtivo() {
        CreateRentalRequest request = new CreateRentalRequest(UUID.randomUUID());
        when(rentalRepository.existsByUserIdAndStatusIn(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> rentalService.criar(user, request))
            .isInstanceOf(RentalEmAndamentoException.class);

        verify(rentalRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(any(), any(), any(Object.class));
    }

    @Test
    @DisplayName("processarResultadoAnalise: REJECTED marca como REJEITADO e publica RentalFailed")
    void processarRejeicaoDeveMarcarRejeitadoEPublicarFailed() {
        UUID rentalId = UUID.randomUUID();
        Rental rental = rentalBase(rentalId);
        AnalysisCompletedEvent event = AnalysisCompletedEvent.rejected(rentalId, "CPF na blacklist");

        when(processedEventRepository.existsById(event.eventId())).thenReturn(false);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));

        rentalService.processarResultadoAnalise(event);

        assertThat(rental.getStatus()).isEqualTo(StatusRental.REJEITADO);
        assertThat(rental.getMotivoFalha()).isEqualTo("CPF na blacklist");
        verify(rabbitTemplate).convertAndSend(
            eq(Exchanges.RENTAL),
            eq(RoutingKeys.RENTAL_FAILED),
            any(RentalFailedEvent.class)
        );
        verify(carServiceClient, never()).reservar(any());
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    @DisplayName("processarResultadoAnalise: APPROVED + Feign sucesso marca CONFIRMADO e publica RentalConfirmed")
    void processarAprovacaoComSucessoDeveMarcarConfirmadoEPublicarConfirmed() {
        UUID rentalId = UUID.randomUUID();
        Rental rental = rentalBase(rentalId);
        AnalysisCompletedEvent event = AnalysisCompletedEvent.approved(rentalId);

        when(processedEventRepository.existsById(event.eventId())).thenReturn(false);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(carServiceClient.reservar(rental.getCarroId())).thenReturn(carroResponseFake());

        rentalService.processarResultadoAnalise(event);

        assertThat(rental.getStatus()).isEqualTo(StatusRental.CONFIRMADO);
        verify(rabbitTemplate).convertAndSend(
            eq(Exchanges.RENTAL),
            eq(RoutingKeys.RENTAL_CONFIRMED),
            any(RentalConfirmedEvent.class)
        );
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    @DisplayName("processarResultadoAnalise: APPROVED + CarroIndisponivel marca FALHOU e publica RentalFailed")
    void processarAprovacaoComCarroIndisponivelDeveMarcarFalhouEPublicarFailed() {
        UUID rentalId = UUID.randomUUID();
        Rental rental = rentalBase(rentalId);
        AnalysisCompletedEvent event = AnalysisCompletedEvent.approved(rentalId);

        when(processedEventRepository.existsById(event.eventId())).thenReturn(false);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(carServiceClient.reservar(rental.getCarroId()))
            .thenThrow(new CarroIndisponivelException());

        rentalService.processarResultadoAnalise(event);

        assertThat(rental.getStatus()).isEqualTo(StatusRental.FALHOU);
        assertThat(rental.getMotivoFalha()).contains("não está mais disponível");
        verify(rabbitTemplate).convertAndSend(
            eq(Exchanges.RENTAL),
            eq(RoutingKeys.RENTAL_FAILED),
            any(RentalFailedEvent.class)
        );
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    @DisplayName("processarResultadoAnalise: evento duplicado deve ser ignorado silenciosamente")
    void processarEventoDuplicadoDeveSerIgnorado() {
        AnalysisCompletedEvent event = AnalysisCompletedEvent.approved(UUID.randomUUID());
        when(processedEventRepository.existsById(event.eventId())).thenReturn(true);

        rentalService.processarResultadoAnalise(event);

        verify(rentalRepository, never()).findById(any());
        verify(carServiceClient, never()).reservar(any());
        verify(rabbitTemplate, never()).convertAndSend(any(), any(), any(Object.class));
        verify(processedEventRepository, never()).save(any(ProcessedEvent.class));
    }

    @Test
    @DisplayName("processarResultadoAnalise: rental inexistente lança RentalNaoEncontradoException")
    void processarRentalInexistenteDeveLancarException() {
        AnalysisCompletedEvent event = AnalysisCompletedEvent.approved(UUID.randomUUID());
        when(processedEventRepository.existsById(event.eventId())).thenReturn(false);
        when(rentalRepository.findById(event.rentalId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.processarResultadoAnalise(event))
            .isInstanceOf(RentalNaoEncontradoException.class);

        verify(processedEventRepository, never()).save(any(ProcessedEvent.class));
    }

    // ----- helpers -----

    private Rental rentalBase(UUID id) {
        return Rental.builder()
            .id(id)
            .userId(user.id())
            .userEmail(user.email())
            .carroId(UUID.randomUUID())
            .status(StatusRental.PENDENTE)
            .criadoEm(Instant.now())
            .atualizadoEm(Instant.now())
            .build();
    }

    private CarroResponse carroResponseFake() {
        return new CarroResponse(
            UUID.randomUUID(), "ABC1234", "Civic", "Honda",
            "Preto", 2023, new BigDecimal("220.00"),
            "RESERVADO", "Reservado"
        );
    }

    private Rental devolverComoSeFosseSalvo(org.mockito.invocation.InvocationOnMock invocation) {
        Rental input = invocation.getArgument(0);
        return Rental.builder()
            .id(input.getId() != null ? input.getId() : UUID.randomUUID())
            .userId(input.getUserId())
            .userEmail(input.getUserEmail())
            .carroId(input.getCarroId())
            .status(input.getStatus())
            .criadoEm(Instant.now())
            .atualizadoEm(Instant.now())
            .build();
    }
}
