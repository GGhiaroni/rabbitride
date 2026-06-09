package com.gghiaroni.rabbitride.rentalservice;

import com.gghiaroni.rabbitride.commons.messaging.AmqpJsonConfig;
import com.gghiaroni.rabbitride.commons.messaging.RabbitTopologyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableFeignClients
@Import({AmqpJsonConfig.class, RabbitTopologyConfig.class})
public class RentalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalServiceApplication.class, args);
    }
}
