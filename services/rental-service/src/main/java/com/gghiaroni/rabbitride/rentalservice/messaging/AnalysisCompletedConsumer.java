package com.gghiaroni.rabbitride.rentalservice.messaging;

import com.gghiaroni.rabbitride.commons.events.AnalysisCompletedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import com.gghiaroni.rabbitride.rentalservice.rental.RentalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisCompletedConsumer {
    private static final Logger log = LoggerFactory.getLogger(AnalysisCompletedConsumer.class);
    private final RentalService rentalService;

    public AnalysisCompletedConsumer(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @RabbitListener(queues = Queues.RENTAL_ANALYSIS_COMPLETED)
    public void onAnalysisCompleted(AnalysisCompletedEvent event){
        log.info("Recebido AnalysisCompleted: rentalId={}, resultado={}", event.rentalId(), event.resultado());
        rentalService.processarResultadoAnalise(event);
    }
}
