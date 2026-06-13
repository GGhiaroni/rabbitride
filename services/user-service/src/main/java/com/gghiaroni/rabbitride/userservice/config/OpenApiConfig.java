package com.gghiaroni.rabbitride.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RabbitRide | user-service")
                .description("Cadastro, autenticação e emissão de JWT.")
                .version("0.1.0")
                .contact(new Contact()
                    .name("Gabriel Tiziano")
                    .email("gghiaronitiziano@gmail.com")
                    .url("https://github.com/GabrielTiziano"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
