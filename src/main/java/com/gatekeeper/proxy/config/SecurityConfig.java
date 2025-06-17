package com.gatekeeper.proxy.config;

import com.gatekeeper.proxy.security.KeycloakAuthenticationSuccessHandler;
import com.gatekeeper.proxy.security.KeycloakLogoutHandler;
import com.gatekeeper.proxy.security.RedisAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakAuthenticationSuccessHandler keycloakAuthenticationSuccessHandler;
    private final KeycloakLogoutHandler keycloakLogoutHandler;
    private final RedisAuthenticationFilter redisAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers("/auth/**").permitAll()
                        // 캐시삭제용 엔드포인트 
                        .pathMatchers("/auth-clear/**").permitAll()
                        .pathMatchers("/auth-check/**").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(keycloakAuthenticationSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutHandler(keycloakLogoutHandler)
                        .logoutSuccessHandler(logoutSuccessHandler())
                )
                .addFilterBefore(redisAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public ServerLogoutSuccessHandler logoutSuccessHandler() {
        RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
        successHandler.setLogoutSuccessUrl(URI.create("/"));
        return successHandler;
    }
} 