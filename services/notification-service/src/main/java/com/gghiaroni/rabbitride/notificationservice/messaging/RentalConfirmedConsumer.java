package com.gghiaroni.rabbitride.notificationservice.messaging;

import com.gghiaroni.rabbitride.commons.events.RentalConfirmedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import com.gghiaroni.rabbitride.notificationservice.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RentalConfirmedConsumer {
    private static final Logger log = LoggerFactory.getLogger(RentalConfirmedConsumer.class);
    private static final String CONSUMER_NAME = "notification-service.RentalConfirmedConsumer";

    private final EmailSender emailSender;

    public RentalConfirmedConsumer(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @RabbitListener(queues = Queues.NOTIFICATION_CONFIRMED)
    public void onRentalConfirmed(RentalConfirmedEvent event) {
        log.info("Recebido RentalConfirmed: eventId={}, rentalId={}, userEmail={}",
            event.eventId(), event.rentalId(), event.userEmail());

        String subject = "Sua reserva foi confirmada";
        String body = montarCorpo(event);

        emailSender.send(event.userEmail(), subject, body);
    }

    private String montarCorpo(RentalConfirmedEvent event) {
        return String.format("""
            Olá!

            Sua reserva foi confirmada com sucesso.

            Detalhes:
              ID da reserva: %s
              Carro: %s

            Em breve enviaremos informações sobre a retirada.

            Equipe RabbitRide
            """,
            event.rentalId(),
            event.carroDescricao()
        );
    }
}
