package com.gghiaroni.rabbitride.analysisservice;

import com.gghiaroni.rabbitride.analysisservice.messaging.ProcessedEventRepository;
import com.gghiaroni.rabbitride.commons.events.AnalysisCompletedEvent;
import com.gghiaroni.rabbitride.commons.events.RentalRequestedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Exchanges;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import com.gghiaroni.rabbitride.commons.messaging.RoutingKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class AnalysisIdempotencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit =
        new RabbitMQContainer("rabbitmq:3-management-alpine");

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void limpar() {
        processedEventRepository.deleteAll();
        rabbitAdmin.purgeQueue(Queues.ANALYSIS_REQUESTED, true);
        rabbitAdmin.purgeQueue(Queues.RENTAL_ANALYSIS_COMPLETED, true);
        rabbitAdmin.purgeQueue(Queues.RENTAL_DLQ, true);
    }

    @Test
    void deveProcessarSomenteUmaVezQuandoMensagemDuplicada() {
        // given — evento construído manualmente pra controlar o eventId
        UUID eventId = UUID.randomUUID();
        UUID rentalId = UUID.randomUUID();
        RentalRequestedEvent event = new RentalRequestedEvent(
            eventId,
            Instant.now(),
            rentalId,
            UUID.randomUUID(),
            "user@test.com",
            "99988877766",          // CPF NÃO está na blacklist
            UUID.randomUUID()
        );

        // when — publica a MESMA mensagem 2x
        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL,
            RoutingKeys.RENTAL_REQUESTED,
            event
        );
        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL,
            RoutingKeys.RENTAL_REQUESTED,
            event
        );

        // then — primeira mensagem deve gerar AnalysisCompleted
        Object firstReceived = rabbitTemplate.receiveAndConvert(
            Queues.RENTAL_ANALYSIS_COMPLETED,
            10_000
        );
        assertThat(firstReceived)
            .as("Esperava AnalysisCompleted da primeira mensagem")
            .isNotNull()
            .isInstanceOf(AnalysisCompletedEvent.class);

        AnalysisCompletedEvent completed = (AnalysisCompletedEvent) firstReceived;
        assertThat(completed.rentalId()).isEqualTo(rentalId);

        // segunda mensagem NÃO deve gerar AnalysisCompleted (foi ignorada)
        Object secondReceived = rabbitTemplate.receiveAndConvert(
            Queues.RENTAL_ANALYSIS_COMPLETED,
            5_000
        );
        assertThat(secondReceived)
            .as("Mensagem duplicada deveria ter sido ignorada — nenhum segundo AnalysisCompleted esperado")
            .isNull();

        // processed_event deve ter exatamente 1 linha
        assertThat(processedEventRepository.count())
            .as("processed_event deve registrar a mensagem apenas 1x")
            .isEqualTo(1);
    }
}
