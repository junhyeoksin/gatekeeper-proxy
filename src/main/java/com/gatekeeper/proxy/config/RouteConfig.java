package com.gatekeeper.proxy.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Wendy's Healing Recipe API 라우팅
                .route("wendy_api_route", r -> r
                        .path("/v1/**")
                        .uri("https://curon.keystrom.site"))
                        
                // 로컬 백엔드 라우팅 (개발용)
                .route("local_backend", r -> r
                        .path("/local-api/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("http://localhost:8081"))
                        
                // Keycloak 관리 API 라우팅
                .route("keycloak_admin", r -> r
                        .path("/auth-admin/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("https://your-keycloak-server/auth"))
                .build();
    }
} 