package com.ticketmaster.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticketmaster API")
                        .description("""
                                API RESTful para gerenciamento de eventos, ingressos e pedidos.
                                
                                ## Autenticação
                                Crie um usuário via POST /usuarios para receber sua **X-API-Key**.
                                Use essa chave no header de todas as requisições POST, PUT e DELETE.
                                
                                ## Idempotência
                                Todas as requisições POST exigem o header **X-Idempotency-Key** (UUID único por operação).
                                Reenviar a mesma chave retorna a resposta original sem reprocessar.
                                
                                ## Versionamento
                                Use **X-API-Version: 1** (padrão) ou **X-API-Version: 2** nos endpoints de Eventos.
                                V1 retorna resposta completa com HATEOAS. V2 retorna formato simplificado.
                                
                                ## Rate Limiting
                                Máximo de 20 requisições por minuto por IP.
                                Ao exceder, retorna HTTP 429 com o header **Retry-After** (segundos).
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Yasmin Carolina")
                                .email("contato@ticketmaster-api.com")))
                .components(new Components()
                        .addSecuritySchemes("X-API-Key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("Chave de autenticação. Crie um usuário via POST /usuarios para obtê-la.")))
                .addSecurityItem(new SecurityRequirement().addList("X-API-Key"));
    }

    @Bean
    public OperationCustomizer globalHeadersCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {

            String httpMethod = handlerMethod.getMethod().getAnnotation(
                    org.springframework.web.bind.annotation.PostMapping.class) != null ? "POST" : null;
            if (httpMethod != null) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-Idempotency-Key")
                        .description("UUID único por operação. Previne processamento duplicado. Obrigatório em POST.")
                        .required(true)
                        .schema(new StringSchema().example("550e8400-e29b-41d4-a716-446655440000")));
            }

            String controllerName = handlerMethod.getBeanType().getSimpleName();
            if ("EventoController".equals(controllerName)) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-API-Version")
                        .description("Versão da resposta: 1 = completo com HATEOAS (padrão), 2 = simplificado sem artistas.")
                        .required(false)
                        .schema(new StringSchema()
                                ._enum(java.util.List.of("1", "2"))
                                ._default("1")));
            }

            return operation;
        };
    }
}
