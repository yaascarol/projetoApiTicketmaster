# Ticketmaster API

API REST desenvolvida para a disciplina de **Desenvolvimento de Web Services**, para o gerenciamento de venda de ingressos e consumo de dados de eventos musicais.

A aplicação está hospedada na nuvem e pode ser testada diretamente pelo navegador:

> **Live:** https://projetoapiticketmaster.onrender.com/swagger-ui/index.html

---

## O que a API faz

O sistema permite gerenciar o ciclo de vida completo de uma venda de ingressos:

- **Consulta**: Busca de eventos, artistas, ingressos e pedidos com paginação completa
- **Cadastro**: Registro de usuários, artistas, eventos e ingressos
- **Venda**: Abertura de pedidos vinculando usuários aos ingressos
- **Autenticação**: Sistema de API Keys com níveis de acesso (READ, WRITE, ADMIN)
- **Segurança de Processamento**: Idempotência que evita cobranças e cadastros duplicados
- **Versionamento**: Suporte a `v1` (resposta simplificada) e `v2` (resposta completa com HATEOAS)
- **Rate Limiting**: Proteção contra excesso de requisições por IP

---

## Tecnologias utilizadas

| Tecnologia | Uso |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 3.2.5 | Framework web |
| Spring Data JPA | Persistência de dados |
| Spring HATEOAS | Links de navegação nas respostas |
| Spring Validation | Validação dos campos de entrada |
| Bucket4j | Rate limiting profissional |
| Springdoc OpenAPI | Documentação automática (Swagger UI) |
| H2 Database | Banco de dados em memória |
| Lombok | Redução de código boilerplate |
| Docker | Containerização |
| Render | Hospedagem na nuvem |

---

## Arquitetura do código

```
src/main/java/com/ticketmaster/api/
├── apikey/          → Entidade, controller, filtro e repositório de API Keys
├── assemblers/      → ModelAssemblers para HATEOAS (um por recurso)
├── config/          → Configuração do Swagger/OpenAPI
├── controller/      → Endpoints REST de cada recurso
├── dto/
│   ├── request/     → Objetos de entrada (o que o cliente envia)
│   └── response/    → Objetos de saída (o que a API retorna)
├── exception/       → Exceções customizadas e handler global de erros
├── filter/          → Filtro de idempotência
├── infrastructure/  → Dados iniciais carregados na inicialização
├── model/           → Entidades JPA (tabelas do banco)
├── ratelimit/       → Configuração e filtro de rate limiting
├── repository/      → Interfaces de comunicação com o banco
└── versioning/      → Interceptor de versionamento e configuração de CORS
```

---

## Funcionalidades e endpoints

| Recurso | Endpoint | Descrição |
|---|---|---|
| API Keys | `/api/auth/api-keys` | Gerar, listar, buscar e revogar chaves |
| Artistas | `/artistas` | CRUD completo com busca por nome e gênero |
| Eventos | `/eventos` | CRUD completo com busca por nome e status |
| Ingressos | `/ingressos` | CRUD completo com busca por tipo e preço |
| Pedidos | `/pedidos` | CRUD completo com busca por usuário e status |
| Itens de Pedido | `/itens-pedido` | CRUD completo com busca por pedido |
| Usuários | `/usuarios` | CRUD completo com busca por nome |
| Swagger UI | `/swagger-ui.html` | Documentação interativa |
| H2 Console | `/h2-console` | Interface do banco de dados |

---

## Headers da API

| Header | Obrigatório em | Descrição |
|---|---|---|
| `X-API-Key` | POST, PUT, DELETE | Chave de autenticação. Gere em `POST /api/auth/api-keys` |
| `X-Idempotency-Key` | POST | UUID único por operação. Evita processamento duplicado |
| `X-API-Version` | Opcional | `v1` = simplificado · `v2` = completo com HATEOAS (padrão) |

---

## Rate Limiting

| Tipo de operação | Limite |
|---|---|
| GET | 30 requisições por minuto por IP |
| POST / PUT / DELETE | 10 requisições por minuto por IP |

Ao exceder o limite, a API retorna **HTTP 429** com o header `Retry-After` indicando quantos segundos aguardar.

---

## Dados pré-carregados

A API já inicializa com dados de exemplo prontos para teste:

- 🎤 **6 artistas**: Taylor Swift, Coldplay, Beyoncé, Bad Bunny, Anitta, Caetano Veloso
- 🎪 **5 eventos** com datas, locais e artistas vinculados
- 🎟️ **12 ingressos** com tipos variados (PISTA, VIP, CAMAROTE, CADEIRA, MEIA_ENTRADA)
- 👤 **3 usuários** de exemplo

---

## Rodando o projeto sem Docker

1. Instale o **JDK 21**
2. Clone o repositório:
```bash
git clone https://github.com/yaascarol/projetoApiTicketmaster.git
```
3. No IntelliJ, ative o **Annotation Processing** em `Settings > Compiler` para o Lombok funcionar
4. No terminal da pasta, rode:
```bash
./mvnw spring-boot:run
```
5. Acesse em: http://localhost:8080/swagger-ui.html

---

## Rodando o projeto com Docker

Se você tiver o Docker instalado, pode rodar o projeto sem instalar o Java:

```bash
# Constrói a imagem
docker build -t ticket-api .

# Inicia o container
docker run -p 8080:8080 ticket-api
```

Acesse em: http://localhost:8080/swagger-ui.html

---

## Desenvolvido por

**Yasmin Carolina** — Disciplina de Desenvolvimento de Web Services
