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
                                API RESTful para gerenciamento de eventos, artistas, ingressos e pedidos de uma plataforma de venda de ingressos.
                                Esta API utiliza autenticação por chave de acesso no header `X-API-Key`.

                                **Níveis de acesso disponíveis:**
                                - `READ` → permite apenas consultas (GET)
                                - `WRITE` → permite consultas e alterações (GET, POST, PUT, DELETE)
                                - `ADMIN` → acesso completo, incluindo revogar outras chaves

                                A chave deve ser enviada no header de todas as requisições de escrita:
                                ```
                                X-API-Key: sua-chave-aqui
                                ```

                                ---
                                ## Códigos de erro

                                | Código | Significado |
                                |--------|-------------|
                                | 400 | Dados inválidos ou header obrigatório ausente |
                                | 401 | X-API-Key ausente, inválida ou revogada |
                                | 403 | Chave sem permissão para esta operação |
                                | 404 | Recurso não encontrado |
                                | 409 | Conflito — recurso já existe |
                                | 422 | Erro de regra de negócio |
                                | 429 | Limite de requisições excedido |
                                | 500 | Erro interno do servidor |
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
                        .name("Idempotency-Key")
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
