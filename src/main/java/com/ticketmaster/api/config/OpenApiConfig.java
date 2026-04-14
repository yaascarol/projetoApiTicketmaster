package com.ticketmaster.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticketmaster API")
                        .description("""
                                API RESTful para gerenciamento de eventos, ingressos e pedidos.
                                Permite criar e consultar eventos, comprar ingressos e acompanhar pedidos de forma paginada.
                                Integra com a API oficial da Ticketmaster para importação de eventos externos.
                                Desenvolvido para a disciplina de Web Services — SENAC TSI
                                Desenvolvedora: Yasmin Carolina.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe Ticketmaster API")
                                .email("contato@ticketmaster-api.com")));
    }
}
