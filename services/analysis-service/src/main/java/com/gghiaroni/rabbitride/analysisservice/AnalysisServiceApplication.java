package com.gghiaroni.rabbitride.analysisservice;

import com.gghiaroni.rabbitride.commons.messaging.AmqpJsonConfig;
import com.gghiaroni.rabbitride.commons.messaging.RabbitTopologyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AmqpJsonConfig.class, RabbitTopologyConfig.class})
public class AnalysisServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalysisServiceApplication.class, args);
    }
}
