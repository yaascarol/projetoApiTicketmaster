package com.ticketmaster.api.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
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
import java.util.Set;

/**
 * Filtro de rate limiting por IP usando Bucket4j (token bucket algorithm).
 *
 * Headers de resposta:
 *   X-Rate-Limit-Remaining          → tokens restantes após a requisição
 *   Retry-After                     → segundos até próximo token (só no 429)
 *   X-Rate-Limit-Retry-After-Seconds → igual ao Retry-After
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> WRITE_METHODS = Set.of(
            HttpMethod.POST.name(), HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(), HttpMethod.DELETE.name()
    );

    private final RateLimitConfig rateLimitConfig;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
                || path.startsWith("/api-docs")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String ip      = resolveIp(request);
        boolean isWrite = WRITE_METHODS.contains(request.getMethod().toUpperCase());

        Bucket bucket          = rateLimitConfig.resolveBucket(ip, isWrite);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
            return;
        }

        long retryAfter = probe.getNanosToWaitForRefill() / 1_000_000_000L;
        log.warn("Rate limit excedido — ip={} method={} retryAfter={}s",
                ip, request.getMethod(), retryAfter);

        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfter));
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":429,\"erro\":\"Too Many Requests\"," +
                "\"mensagem\":\"Limite de requisições excedido. Tente novamente em %d segundo(s).\"," +
                "\"path\":\"%s\"}",
                Instant.now(), retryAfter, request.getRequestURI()));
    }

    /** Resolve IP real mesmo atrás de proxy/load balancer. */
    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank())
            return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
