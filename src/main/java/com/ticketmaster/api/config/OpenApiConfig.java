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

                                ---

                                ## 🔑 Autenticação — API Keys

                                Esta API utiliza autenticação por chave de acesso no header `X-API-Key`.

                                **Como obter sua chave:**
                                1. Acesse `POST /api/auth/api-keys`
                                2. Informe seu nome (`owner`) e o nível de acesso desejado
                                3. Copie o valor do campo `apiKey` retornado
                                4. Clique em **Authorize** (🔒) no topo desta página e cole a chave

                                **Níveis de acesso disponíveis:**
                                - `READ` → permite apenas consultas (GET)
                                - `WRITE` → permite consultas e alterações (GET, POST, PUT, DELETE)
                                - `ADMIN` → acesso completo, incluindo revogar outras chaves

                                A chave deve ser enviada no header de todas as requisições de escrita:
                                ```
                                X-API-Key: sua-chave-aqui
                                ```

                                ---

                                ## 🔁 Idempotência

                                Todas as requisições `POST` exigem o header `Idempotency-Key` com um UUID único por operação.

                                **Como funciona:**
                                - Na primeira vez que a chave é enviada, a requisição é processada normalmente
                                - Se a mesma chave for enviada novamente, a API retorna a resposta original **sem reprocessar**
                                - Isso evita cobranças ou cadastros duplicados em caso de falha de rede ou clique duplo

                                **Exemplo de chave válida:**
                                ```
                                Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
                                ```

                                Gere um UUID em: https://www.uuidgenerator.net

                                ---

                                ## 🔀 Versionamento

                                A API suporta duas versões de resposta via header `X-API-Version`:

                                | Versão | Comportamento |
                                |--------|--------------|
                                | `v2`   | Resposta completa com HATEOAS, links de navegação e metadados de paginação *(padrão)* |
                                | `v1`   | Resposta simplificada, apenas campos essenciais, sem links |

                                Versões inválidas retornam **400 Bad Request** com mensagem explicativa.

                                O header `X-API-Version` é ecoado na resposta para confirmar qual versão foi utilizada.

                                ---

                                ## 🚦 Rate Limiting

                                A API limita o número de requisições por IP para garantir estabilidade:

                                | Tipo de operação | Limite |
                                |-----------------|--------|
                                | `GET` | 10 requisições por minuto |
                                | `POST` / `PUT` / `DELETE` | 5 requisições por minuto |

                                Ao exceder o limite, a API retorna **429 Too Many Requests** com os headers:
                                - `Retry-After` → segundos até a próxima requisição ser aceita
                                - `X-Rate-Limit-Retry-After-Seconds` → igual ao Retry-After

                                ---

                                ## 📄 Paginação

                                Todos os endpoints de listagem suportam paginação via parâmetros de query:

                                | Parâmetro | Descrição | Exemplo |
                                |-----------|-----------|---------|
                                | `page` | Número da página (começa em 0) | `?page=0` |
                                | `size` | Itens por página | `?size=10` |
                                | `sort` | Campo e direção de ordenação | `?sort=nome,asc` |

                                A resposta inclui `totalElements`, `totalPages`, e links HATEOAS de navegação (`first`, `prev`, `next`, `last`).

                                ---

                                ## ⚡ Dados pré-carregados

                                A API já inicializa com dados de exemplo prontos para teste:
                                - **6 artistas**: Taylor Swift, Coldplay, Beyoncé, Bad Bunny, Anitta, Caetano Veloso
                                - **5 eventos** com datas, locais e artistas vinculados
                                - **12 ingressos** com tipos variados (PISTA, VIP, CAMAROTE, CADEIRA, MEIA_ENTRADA)
                                - **3 usuários** de exemplo

                                ---

                                ## ❌ Códigos de erro

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
