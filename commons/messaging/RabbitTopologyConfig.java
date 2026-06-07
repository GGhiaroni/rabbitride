package com.gghiaroni.rabbitride.commons.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    @Bean
    public TopicExchange rentalExchange() {
        return new TopicExchange(Exchanges.RENTAL, true, false);
    }
}
