package com.ticketmaster.api.apikey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

/**
 * Filtro que valida o header X-API-Key e aplica controle de acesso por nível.
 *
 * Rotas PÚBLICAS (sem chave):
 *   - GET  qualquer rota         → leitura livre
 *   - POST /api/auth/api-keys    → geração de chaves
 *   - POST /usuarios             → cadastro de usuários
 *   - /swagger-ui /api-docs /h2-console
 *
 * Rotas PROTEGIDAS:
 *   - POST/PUT/PATCH/DELETE → exige chave com nível WRITE ou ADMIN
 *   - DELETE /api/auth/api-keys/{id} → exige nível ADMIN
 *
 * Respostas de erro:
 *   401 → chave ausente ou inválida/revogada
 *   403 → chave válida mas sem permissão para o método
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Set<String> WRITE_METHODS = Set.of(
            HttpMethod.POST.name(), HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(), HttpMethod.DELETE.name()
    );

    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod().toUpperCase();
        String path   = request.getRequestURI();

        // GETs são públicos
        if (!WRITE_METHODS.contains(method)) return true;

        // Geração de chaves é pública
        if (path.equals("/api/auth/api-keys") && method.equals("POST")) return true;

        // Cadastro de usuário é público
        if (path.equals("/usuarios") && method.equals("POST")) return true;

        // Infraestrutura
        return path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.startsWith("/actuator")
                || method.equals("OPTIONS");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String rawKey = request.getHeader("X-API-Key");
        String method = request.getMethod().toUpperCase();
        String path   = request.getRequestURI();

        // 1. Header ausente
        if (rawKey == null || rawKey.isBlank()) {
            reject(response, request, 401,
                    "Header X-API-Key ausente. Gere uma chave em POST /api/auth/api-keys.");
            return;
        }

        // 2. Chave inválida ou revogada
        Optional<ApiKey> opt = apiKeyRepository.findByKeyValueAndActiveTrue(rawKey);
        if (opt.isEmpty()) {
            log.warn("X-API-Key inválida/revogada — path={} key={}", path, rawKey);
            reject(response, request, 401, "X-API-Key inválida ou revogada.");
            return;
        }

        ApiKey key = opt.get();
        ApiKey.AccessLevel level = key.getAccessLevel();

        // 3. READ tenta fazer escrita → 403
        if (level == ApiKey.AccessLevel.READ) {
            log.warn("Acesso negado — chave READ tentou {} {}", method, path);
            reject(response, request, 403,
                    "Sua chave tem nível READ e não pode realizar operações de escrita.");
            return;
        }

        // 4. WRITE tenta revogar chaves → só ADMIN pode
        if (level == ApiKey.AccessLevel.WRITE
                && method.equals("DELETE")
                && path.startsWith("/api/auth/api-keys/")) {
            log.warn("Acesso negado — chave WRITE tentou revogar outra chave");
            reject(response, request, 403,
                    "Apenas chaves com nível ADMIN podem revogar outras chaves.");
            return;
        }

        log.info("X-API-Key válida — level={} method={} path={}", level, method, path);
        response.setHeader("X-API-Key-Level", level.name());
        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, HttpServletRequest request,
                        int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String error = status == 401 ? "Unauthorized" : "Forbidden";
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                Instant.now(), status, error, message, request.getRequestURI()));
    }
}
