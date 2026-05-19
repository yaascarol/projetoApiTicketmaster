package com.ticketmaster.api.filter;

import com.ticketmaster.api.model.IdempotencyRecord;
import com.ticketmaster.api.repository.IdempotencyRecordRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Order(3)
public class IdempotencyFilter extends OncePerRequestFilter {

    @Autowired
    private IdempotencyRecordRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        if (deveIgnorar(uri)) {
            chain.doFilter(request, response);
            return;
        }

        String chave = request.getHeader("X-Idempotency-Key");

        if (chave == null || chave.isBlank()) {
            response.setStatus(400);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"timestamp\":\"" + LocalDateTime.now() + "\"," +
                "\"status\":400," +
                "\"erro\":\"Header X-Idempotency-Key é obrigatório para requisições POST\"}"
            );
            return;
        }

        Optional<IdempotencyRecord> existente = repository.findById(chave);
        if (existente.isPresent()) {
            IdempotencyRecord record = existente.get();
            response.setStatus(record.getStatusHttp());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(record.getCorpoResposta());
            return;
        }

        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        chain.doFilter(request, wrappedResponse);

        byte[] conteudo = wrappedResponse.getContentAsByteArray();
        String corpoResposta = new String(conteudo, StandardCharsets.UTF_8);

        IdempotencyRecord novoRecord = new IdempotencyRecord();
        novoRecord.setChave(chave);
        novoRecord.setStatusHttp(wrappedResponse.getStatus());
        novoRecord.setCorpoResposta(corpoResposta);
        novoRecord.setCriadoEm(LocalDateTime.now());
        repository.save(novoRecord);

        wrappedResponse.copyBodyToResponse();
    }

    private boolean deveIgnorar(String uri) {
        return uri.startsWith("/swagger-ui")
            || uri.startsWith("/api-docs")
            || uri.startsWith("/v3/api-docs")
            || uri.startsWith("/h2-console")
            || uri.equals("/usuarios");
    }
}
