package com.ticketmaster.api.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing).
 *
 * Permite que frontends em origens distintas consumam a API,
 * controlando quais origens, métodos e headers são aceitos.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedOriginPattern("*");

        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("Accept");
        config.addAllowedHeader("X-API-Key");
        config.addAllowedHeader("Idempotency-Key");
        config.addAllowedHeader("X-API-Version");
        config.addAllowedHeader("Authorization");

        config.addExposedHeader("X-Rate-Limit-Remaining");
        config.addExposedHeader("X-Rate-Limit-Retry-After-Seconds");
        config.addExposedHeader("Retry-After");
        config.addExposedHeader("Location");
        config.addExposedHeader("X-API-Key-Level");
        config.addExposedHeader("X-API-Version");

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
