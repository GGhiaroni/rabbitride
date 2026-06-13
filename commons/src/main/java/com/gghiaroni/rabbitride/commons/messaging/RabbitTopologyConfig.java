package com.gghiaroni.rabbitride.commons.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    //exchange principal
    @Bean
    public TopicExchange rentalExchange() {
        return new TopicExchange(Exchanges.RENTAL, true, false);
    }

    //queues (todas durables, com DLX configurada)
    @Bean
    public Queue analysisRequestedQueue() {
        return QueueBuilder.durable(Queues.ANALYSIS_REQUESTED)
            .withArgument("x-dead-letter-exchange", Exchanges.RENTAL_DLX)
            .build();
    }

    @Bean
    public Queue rentalAnalysisCompletedQueue() {
        return QueueBuilder.durable(Queues.RENTAL_ANALYSIS_COMPLETED)
            .withArgument("x-dead-letter-exchange", Exchanges.RENTAL_DLX)
            .build();
    }

    //bindings
    @Bean
    public Binding analysisRequestedBinding(Queue analysisRequestedQueue, TopicExchange rentalExchange) {
        return BindingBuilder
            .bind(analysisRequestedQueue)
            .to(rentalExchange)
            .with(RoutingKeys.RENTAL_REQUESTED);
    }

    @Bean
    public Binding rentalAnalysisCompletedBinding(Queue rentalAnalysisCompletedQueue, TopicExchange rentalExchange) {
        return BindingBuilder
            .bind(rentalAnalysisCompletedQueue)
            .to(rentalExchange)
            .with(RoutingKeys.ANALYSIS_COMPLETED);
    }

    //queues — uma por tipo de evento
    @Bean
    public Queue notificationConfirmedQueue() {
        return QueueBuilder.durable(Queues.NOTIFICATION_CONFIRMED)
            .withArgument("x-dead-letter-exchange", Exchanges.RENTAL_DLX)
            .build();
    }

    @Bean
    public Queue notificationFailedQueue() {
        return QueueBuilder.durable(Queues.NOTIFICATION_FAILED)
            .withArgument("x-dead-letter-exchange", Exchanges.RENTAL_DLX)
            .build();
    }

    //bindings — cada queue com sua routing key
    @Bean
    public Binding notificationConfirmedBinding(Queue notificationConfirmedQueue, TopicExchange rentalExchange) {
        return BindingBuilder
            .bind(notificationConfirmedQueue)
            .to(rentalExchange)
            .with(RoutingKeys.RENTAL_CONFIRMED);
    }

    @Bean
    public Binding notificationFailedBinding(Queue notificationFailedQueue, TopicExchange rentalExchange) {
        return BindingBuilder
            .bind(notificationFailedQueue)
            .to(rentalExchange)
            .with(RoutingKeys.RENTAL_FAILED);
    }

    //dead letter exchange + queue
    @Bean
    public TopicExchange rentalDeadLetterExchange() {
        return new TopicExchange(Exchanges.RENTAL_DLX, true, false);
    }

    @Bean
    public Queue rentalDeadLetterQueue() {
        return QueueBuilder.durable(Queues.RENTAL_DLQ).build();
        // Nota: DLQ NÃO tem x-dead-letter-exchange — ela é o destino final
    }

    @Bean
    public Binding rentalDeadLetterBinding(Queue rentalDeadLetterQueue, TopicExchange rentalDeadLetterExchange) {
        return BindingBuilder
            .bind(rentalDeadLetterQueue)
            .to(rentalDeadLetterExchange)
            .with("#");   // captura qualquer routing key
    }
}
