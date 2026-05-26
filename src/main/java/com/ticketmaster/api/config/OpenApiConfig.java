package com.ticketmaster.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

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
                                Gere uma **X-API-Key** via `POST /api/auth/api-keys`.
                                Use essa chave no header de todas as requisições POST, PUT e DELETE.

                                ## Idempotência
                                Todas as requisições POST exigem o header **X-Idempotency-Key** (UUID único).
                                Reenviar a mesma chave retorna a resposta original sem reprocessar.

                                ## Versionamento
                                Use **X-API-Version: v1** (simplificado) ou **v2** (padrão, HATEOAS completo).
                                Versões inválidas retornam 400.

                                ## Rate Limiting
                                GET: 30 req/min por IP · POST/PUT/DELETE: 10 req/min por IP.
                                Ao exceder, retorna 429 com o header **Retry-After** (segundos).
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Ticketmaster API Team")
                                .email("contato@ticketmaster-api.com")))
                .components(new Components()
                        .addSecuritySchemes("X-API-Key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("Chave de autenticação. Gere via POST /api/auth/api-keys.")))
                .addSecurityItem(new SecurityRequirement().addList("X-API-Key"));
    }

    @Bean
    public OperationCustomizer globalHeadersCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {

            boolean isPost = hasAnnotation(handlerMethod,
                    org.springframework.web.bind.annotation.PostMapping.class);

            if (isPost) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-Idempotency-Key")
                        .description("UUID único por operação. Previne reprocessamento duplicado. Obrigatório em POST.")
                        .required(true)
                        .schema(new StringSchema().example("550e8400-e29b-41d4-a716-446655440000")));
            }

            String controller = handlerMethod.getBeanType().getSimpleName();
            if ("EventoController".equals(controller)) {
                operation.addParametersItem(new Parameter()
                        .in("header")
                        .name("X-API-Version")
                        .description("v1 = simplificado · v2 = completo com HATEOAS (padrão).")
                        .required(false)
                        .schema(new StringSchema()
                                ._enum(java.util.List.of("v1", "v2"))
                                ._default("v2")));
            }

            return operation;
        };
    }

    private boolean hasAnnotation(HandlerMethod hm,
                                   Class<? extends Annotation> annotationType) {
        return hm.getMethod().getAnnotation(annotationType) != null;
    }
}
