package com.gatekeeper.proxy.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KeycloakLogoutHandler implements ServerLogoutHandler {

    private final WebClient webClient;
    
    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    public KeycloakLogoutHandler(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            if (authentication.getPrincipal() instanceof OidcUser) {
                OidcUser user = (OidcUser) authentication.getPrincipal();
                String idToken = user.getIdToken().getTokenValue();
                
                
                return callKeycloakLogoutApi(idToken)
                    .doOnSuccess(result -> log.info("로그아웃 성공"))
                    .doOnError(error -> log.error("로그아웃 실패", error))
                    .onErrorResume(e -> Mono.empty());
            }
        }
        
        return Mono.empty();
    }

    private Mono<Void> callKeycloakLogoutApi(String idToken) {
        String endSessionEndpoint = issuerUri + "/protocol/openid-connect/logout";
        
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path(endSessionEndpoint)
                .queryParam("id_token_hint", idToken)
                .build())
            .retrieve()
            .bodyToMono(Void.class);
    }
} 