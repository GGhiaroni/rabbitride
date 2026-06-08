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

    @Bean
    public Queue rentalAnalysisCompletedQueue() {
        return QueueBuilder.durable(Queues.RENTAL_ANALYSIS_COMPLETED)
            .withArgument("x-dead-letter-exchange", Exchanges.RENTAL_DLX)
            .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(Queues.NOTIFICATION)
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

    @Bean
    public Binding notificationConfirmedBinding(Queue notificationQueue, TopicExchange rentalExchange) {
        return BindingBuilder
            .bind(notificationQueue)
            .to(rentalExchange)
            .with(RoutingKeys.RENTAL_CONFIRMED);
    }

    @Bean
    public Binding notificationFailedBinding(Queue notificationQueue, TopicExchange rentalExchange) {
        return BindingBuilder
            .bind(notificationQueue)
            .to(rentalExchange)
            .with(RoutingKeys.RENTAL_FAILED);
    }
}
