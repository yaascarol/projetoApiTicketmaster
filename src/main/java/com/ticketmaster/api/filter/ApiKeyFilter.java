package com.ticketmaster.api.filter;

import com.ticketmaster.api.model.Usuario;
import com.ticketmaster.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Order(2)
public class ApiKeyFilter extends OncePerRequestFilter {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String metodo = request.getMethod();
        String uri = request.getRequestURI();

        boolean ehOperacaoProtegida =
                metodo.equalsIgnoreCase("POST") ||
                metodo.equalsIgnoreCase("PUT") ||
                metodo.equalsIgnoreCase("DELETE");

        if (!ehOperacaoProtegida || deveIgnorar(uri, metodo)) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"timestamp\":\"" + LocalDateTime.now() + "\"," +
                "\"status\":401," +
                "\"erro\":\"Header X-API-Key ausente. Crie um usuário para obter sua chave.\"}"
            );
            return;
        }

        Optional<Usuario> usuario = usuarioRepository.findByApiKey(apiKey);
        if (usuario.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"timestamp\":\"" + LocalDateTime.now() + "\"," +
                "\"status\":401," +
                "\"erro\":\"X-API-Key inválida ou inexistente.\"}"
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean deveIgnorar(String uri, String metodo) {
        if (uri.equals("/usuarios") && metodo.equalsIgnoreCase("POST")) return true;

        return uri.startsWith("/swagger-ui")
            || uri.startsWith("/api-docs")
            || uri.startsWith("/v3/api-docs")
            || uri.startsWith("/h2-console")
            || metodo.equalsIgnoreCase("OPTIONS");
    }
}
