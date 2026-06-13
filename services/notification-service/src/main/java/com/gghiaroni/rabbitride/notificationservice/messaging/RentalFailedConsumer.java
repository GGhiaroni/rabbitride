package com.gghiaroni.rabbitride.notificationservice.messaging;

import com.gghiaroni.rabbitride.commons.events.RentalFailedEvent;
import com.gghiaroni.rabbitride.commons.messaging.Queues;
import com.gghiaroni.rabbitride.notificationservice.email.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RentalFailedConsumer {

    private static final Logger log = LoggerFactory.getLogger(RentalFailedConsumer.class);
    private static final String CONSUMER_NAME = "notification-service.RentalFailedConsumer";

    private final EmailSender emailSender;

    public RentalFailedConsumer(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @RabbitListener(queues = Queues.NOTIFICATION_FAILED)
    public void onRentalFailed(RentalFailedEvent event) {
        log.info("Recebido RentalFailed: eventId={}, rentalId={}, userEmail={}, motivo={}",
            event.eventId(), event.rentalId(), event.userEmail(), event.motivo());

        String subject = "Não foi possível confirmar sua reserva";
        String body = montarCorpo(event);

        emailSender.send(event.userEmail(), subject, body);
    }

    private String montarCorpo(RentalFailedEvent event) {
        return String.format("""
            Olá,

            Infelizmente não foi possível confirmar sua reserva.

            Detalhes:
              ID da reserva: %s
              Motivo: %s

            Você pode tentar uma nova reserva quando preferir.

            Se tiver dúvidas, entre em contato com nosso suporte.

            Equipe RabbitRide
            """,
            event.rentalId(),
            event.motivo()
        );
    }
}
