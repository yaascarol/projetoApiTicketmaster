# Ticketmaster API

Este projeto é uma API REST, desenvolvida para a disciplina de [Desenvolvimento de Web Services], para o gerenciamento de venda de ingressos e consumo de dados de eventos reais através da integração com a API da Ticketmaster.

A aplicação está hospedada na nuvem e pode ser testada diretamente pelo navegador:
> Projeto interativo live em: https://projetoapiticketmaster.onrender.com/swagger-ui/index.html

### O que a API faz:

O sistema permite gerenciar o ciclo de vida de uma venda de ingressos.
* **Consulta Externa**: Busca eventos, datas e locais direto da base da Ticketmaster.
* **Cadastro**: Registro de usuários e tipos de ingressos (Pista, VIP, etc).
* **Venda**: Abertura de pedidos, vinculando o usuário aos itens.
* **Segurança de Processamento**: Implementação de campo para chave que evita cobrança duplicada.

### Tecnologias utilizadas

* **Java 21**
* **Spring Boot 3.4**
* **Spring Data JPA**
* **H2 Database**
* **Lombok**
* **Docker**
* **Render**

### Arquitetura do código

* **Controller**: Exposição dos endpoints REST.
* **Service**: Lógica e integração com a Ticketmaster.
* **Repository**: Interface de comunicação com o banco de dados.
* **Model**: Definição das tabelas do banco.

## Rodando o projeto localmente (sem Docker)

- Instale o JDK 21.
- Clone o repositório: `git clone https://github.com/yaascarol/projetoApiTicketmaster.git`
- No IntelliJ, ative o **Annotation Processing** (em Settings > Compiler) para o Lombok funcionar.
- No terminal da pasta, rode:
```bash
mvn clean spring-boot:run
```
Acesse em: http://localhost:8080/swagger-ui/index.html

## Rodando o projeto com Docker

Se você tiver o Docker instalado, pode rodar o projeto na sua máquina sem instalar o Java, através do terminal utilizando os seguintes comandos:

docker build -t ticket-api .
_ Constrói a imagem _
docker run -p 8080:8080 ticket-api
_ Inicia o container _
Acesse em: http://localhost:8080/swagger-ui/index.html

Desenvolvido por: Yasmin Carolina
