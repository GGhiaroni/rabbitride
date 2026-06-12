package com.gghiaroni.rabbitride.analysisservice.messaging;

import com.gghiaroni.rabbitride.analysisservice.analysis.AnalysisResult;
import com.gghiaroni.rabbitride.analysisservice.analysis.AnalysisService;
import com.gghiaroni.rabbitride.commons.events.AnalysisCompletedEvent;
import com.gghiaroni.rabbitride.commons.events.RentalRequestedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Exchanges;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import com.gghiaroni.rabbitride.commons.messaging.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AnalysisRequestedConsumer {
    private static final Logger log = LoggerFactory.getLogger(AnalysisRequestedConsumer.class);
    private static final String CONSUMER_NAME = "analysis-service.AnalysisRequestedConsumer";

    private final AnalysisService analysisService;
    private final RabbitTemplate rabbitTemplate;
    private final ProcessedEventRepository processedEventRepository;

    public AnalysisRequestedConsumer(AnalysisService analysisService, RabbitTemplate rabbitTemplate, ProcessedEventRepository processedEventRepository) {
        this.analysisService = analysisService;
        this.rabbitTemplate = rabbitTemplate;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    @RabbitListener(queues = Queues.ANALYSIS_REQUESTED)
    public void onRentalRequested(RentalRequestedEvent event) {
        log.info("Recebido RentalRequested: eventId={}, rentalId={}, userId={}, userEmail={}, userCpf={}, carroId={}",
            event.eventId(), event.rentalId(), event.userId(), event.userEmail(), event.userCpf(), event.carroId());

        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Evento já processado, ignorando: eventId={}, rentalId={}",
                event.eventId(), event.rentalId());
            return;
        }

        AnalysisResult resultado = analysisService.analisar(event);

        AnalysisCompletedEvent completedEvent = resultado.aprovado()
            ? AnalysisCompletedEvent.approved(event.rentalId())
            : AnalysisCompletedEvent.rejected(event.rentalId(), resultado.motivo());

        rabbitTemplate.convertAndSend(
            Exchanges.RENTAL,
            RoutingKeys.ANALYSIS_COMPLETED,
            completedEvent
        );

        processedEventRepository.save(new ProcessedEvent(event.eventId(), CONSUMER_NAME));

        log.info("Evento AnalysisCompleted publicado: eventId={}, rentalId={}, resultado={}",
            completedEvent.eventId(), completedEvent.rentalId(), completedEvent.resultado());
    }
}
