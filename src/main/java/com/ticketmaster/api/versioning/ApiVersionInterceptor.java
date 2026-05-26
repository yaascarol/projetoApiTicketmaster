package com.ticketmaster.api.versioning;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Interceptor global de versionamento via header X-API-Version.
 *
 * - Aceita: v1, v2 (ou 1, 2 — normalizado automaticamente)
 * - Padrão quando ausente: v2
 * - Versão desconhecida → 400 Bad Request
 * - Ecoa a versão usada no header de resposta
 */
@Slf4j
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final Set<String> SUPPORTED = Set.of("v1", "v2");

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String raw = request.getHeader("X-API-Version");

        if (raw == null || raw.isBlank()) {
            response.setHeader("X-API-Version", "v2");
            return true;
        }

        // normaliza: "1" → "v1", "2" → "v2"
        String version = raw.trim().toLowerCase();
        if (!version.startsWith("v")) version = "v" + version;

        if (!SUPPORTED.contains(version)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                    "{\"status\":400,\"erro\":\"Versão inválida: '%s'. Versões suportadas: v1, v2.\"}",
                    raw));
            return false;
        }

        log.debug("X-API-Version={} path={}", version, request.getRequestURI());
        response.setHeader("X-API-Version", version);
        return true;
    }
}
