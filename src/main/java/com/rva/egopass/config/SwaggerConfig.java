package com.rva.egopass.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("eGoPass API Documentation")
                        .description("API pour la gestion des e-GoPass RVA")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Équipe de développement RVA")
                                .email("adzabaarbogaste@gmail.com"))
                        .license(new License().name("Propriétaire").url("https://www.rva.cd")))
                .servers(List.of(
                        new Server()
                                .url("https://api.egopass.com")
                                .description("Serveur de production"),
                        new Server()
                                .url("https://staging-api.egopass.com")
                                .description("Serveur de staging"),
                        new Server()
                                .url("http://localhost:8086")
                                .description("Serveur de développement local")
                ));
    }
}


