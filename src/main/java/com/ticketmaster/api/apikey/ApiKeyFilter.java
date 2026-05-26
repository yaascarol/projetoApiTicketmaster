package com.ticketmaster.api.apikey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String method = request.getMethod().toUpperCase();
        String uri    = request.getRequestURI();

        if (!WRITE_METHODS.contains(method) || isPublicRoute(uri, method)) {
            chain.doFilter(request, response);
            return;
        }

        String rawKey = request.getHeader("X-API-Key");

        if (rawKey == null || rawKey.isBlank()) {
            reject(response, 401, "Header X-API-Key ausente. Gere uma chave em POST /api/auth/api-keys.", uri);
            return;
        }

        Optional<ApiKey> found = apiKeyRepository.findByKeyValueAndActiveTrue(rawKey);

        if (found.isEmpty()) {
            reject(response, 401, "X-API-Key inválida, inexistente ou revogada.", uri);
            return;
        }

        ApiKey apiKey = found.get();

        // READ só pode GET — se chegou aqui é escrita, então READ não passa
        if (apiKey.getAccessLevel() == ApiKey.AccessLevel.READ) {
            reject(response, 403, "Sua chave possui nível READ e não permite operações de escrita.", uri);
            return;
        }

        log.info("Autenticado — owner={} level={} method={} uri={}",
                apiKey.getOwner(), apiKey.getAccessLevel(), method, uri);

        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, int status, String message, String path) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"erro\":\"%s\",\"path\":\"%s\"}",
                Instant.now(), status, message, path));
    }

    private boolean isPublicRoute(String uri, String method) {
        if (uri.startsWith("/api/auth/api-keys") && "POST".equals(method)) return true;
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/api-docs")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/h2-console")
                || "OPTIONS".equals(method);
    }
}
