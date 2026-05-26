package com.ticketmaster.api.versioning;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiVersionInterceptor apiVersionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiVersionInterceptor)
                .addPathPatterns("/eventos/**", "/artistas/**",
                                 "/ingressos/**", "/pedidos/**",
                                 "/itens-pedido/**", "/usuarios/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "https://projetoapiticketmaster.onrender.com"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-API-Key", "X-Idempotency-Key", "X-API-Version")
                .exposedHeaders("Retry-After", "X-Rate-Limit-Remaining",
                                "X-Rate-Limit-Retry-After-Seconds", "X-API-Version", "Location")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
