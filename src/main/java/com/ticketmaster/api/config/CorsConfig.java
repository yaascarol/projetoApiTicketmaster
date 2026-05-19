package com.ticketmaster.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "https://projetoapiticketmaster.onrender.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders(
                    "Content-Type",
                    "X-API-Key",
                    "X-Idempotency-Key"
                )
                .exposedHeaders(
                    "Retry-After"
                )
                .allowCredentials(false)
                .maxAge(3600);
    }
}
