package com.gghiaroni.rabbitride.rentalservice.integration;

import com.gghiaroni.rabbitride.commons.events.AnalysisCompletedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Exchanges;
import com.gghiaroni.rabbitride.commons.messaging.RoutingKeys;
import com.gghiaroni.rabbitride.rentalservice.integration.car.CarServiceClient;
import com.gghiaroni.rabbitride.rentalservice.integration.car.CarroResponse;
import com.gghiaroni.rabbitride.rentalservice.messaging.ProcessedEventRepository;
import com.gghiaroni.rabbitride.rentalservice.rental.Rental;
import com.gghiaroni.rabbitride.rentalservice.rental.RentalRepository;
import com.gghiaroni.rabbitride.rentalservice.rental.StatusRental;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class RentalFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private RentalRepository rentalRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;

    @MockBean
    private CarServiceClient carServiceClient;

    @BeforeEach
    void setUp() {
        rentalRepository.deleteAll();
        processedEventRepository.deleteAll();

        when(carServiceClient.reservar(any())).thenReturn(new CarroResponse(
            UUID.randomUUID(), "TST1234", "Civic", "Honda",
            "Preto", 2023, new BigDecimal("220.00"),
            "RESERVADO", "Reservado"
        ));
    }

    @Test
    @DisplayName("Fluxo end-to-end: publicar AnalysisCompleted APPROVED → rental vira CONFIRMADO via @RabbitListener")
    void fluxoCompletoAprovadoDeveConfirmarRental() {
        Rental rental = persistirRental(StatusRental.PENDENTE);
        UUID rentalId = rental.getId();

        AnalysisCompletedEvent evento = AnalysisCompletedEvent.approved(rentalId);
        rabbitTemplate.convertAndSend(Exchanges.RENTAL, RoutingKeys.ANALYSIS_COMPLETED, evento);

        Awaitility.await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                Rental atualizado = rentalRepository.findById(rentalId).orElseThrow();
                assertThat(atualizado.getStatus()).isEqualTo(StatusRental.CONFIRMADO);
            });
    }

    @Test
    @DisplayName("Idempotência: publicar mesmo evento 2x deve processar apenas 1x")
    void evento2xDeveSerProcessadoUmaVez() {
        Rental rental = persistirRental(StatusRental.PENDENTE);
        UUID rentalId = rental.getId();

        AnalysisCompletedEvent evento = AnalysisCompletedEvent.approved(rentalId);

        rabbitTemplate.convertAndSend(Exchanges.RENTAL, RoutingKeys.ANALYSIS_COMPLETED, evento);
        rabbitTemplate.convertAndSend(Exchanges.RENTAL, RoutingKeys.ANALYSIS_COMPLETED, evento);

        Awaitility.await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> {
                assertThat(processedEventRepository.findAll()).hasSize(1);
                assertThat(processedEventRepository.findAll().get(0).getEventId())
                    .isEqualTo(evento.eventId());
            });
    }

    private Rental persistirRental(StatusRental status) {
        Rental rental = Rental.builder()
            .userId(UUID.randomUUID())
            .userEmail("test@integration.com")
            .carroId(UUID.randomUUID())
            .status(status)
            .build();
        return rentalRepository.save(rental);
    }
}
