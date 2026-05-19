package com.ticketmaster.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMITE_REQUISICOES = 20;

    private static final long JANELA_MS = 60_000;

    private final Map<String, Deque<Long>> registros = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (deveIgnorar(uri, request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        long agora = System.currentTimeMillis();

        registros.putIfAbsent(ip, new ArrayDeque<>());
        Deque<Long> timestamps = registros.get(ip);

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && agora - timestamps.peekFirst() > JANELA_MS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= LIMITE_REQUISICOES) {
                long tempoAteReset = (JANELA_MS - (agora - timestamps.peekFirst())) / 1000;

                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.setHeader("Retry-After", String.valueOf(tempoAteReset));
                response.getWriter().write(
                    "{\"timestamp\":\"" + LocalDateTime.now() + "\"," +
                    "\"status\":429," +
                    "\"erro\":\"Too Many Requests — limite de " + LIMITE_REQUISICOES +
                    " requisições por minuto atingido.\"," +
                    "\"retryAfterSegundos\":" + tempoAteReset + "}"
                );
                return;
            }

            timestamps.addLast(agora);
        }

        chain.doFilter(request, response);
    }

    private boolean deveIgnorar(String uri, String metodo) {
        return metodo.equalsIgnoreCase("OPTIONS")
            || uri.startsWith("/swagger-ui")
            || uri.startsWith("/api-docs")
            || uri.startsWith("/v3/api-docs")
            || uri.startsWith("/h2-console");
    }
}
