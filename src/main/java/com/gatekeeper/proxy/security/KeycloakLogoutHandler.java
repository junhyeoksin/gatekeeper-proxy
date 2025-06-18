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
import org.springframework.web.server.WebSession;
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
        if (authentication instanceof OAuth2AuthenticationToken &&
                authentication.getPrincipal() instanceof OidcUser user) {

            String idToken = user.getIdToken().getTokenValue();
            String userId = user.getSubject();
            log.info("로그아웃 요청: userId={}", userId);

            return callKeycloakLogoutApi(idToken)
                    .doOnSuccess(result -> log.info("Keycloak 로그아웃 API 호출 성공"))
                    .doOnError(error -> log.error("Keycloak 로그아웃 API 호출 실패", error))
                    .onErrorResume(e -> Mono.empty())
                    .then(exchange.getExchange().getSession()
                            .flatMap(session -> {
                                log.info("Spring WebSession 무효화 시작");
                                return session.invalidate();
                            }))
                    .doOnSuccess(result -> log.info("전체 로그아웃 프로세스 완료"))
                    .doOnError(error -> log.error("전체 로그아웃 프로세스 실패", error));
        }

        return Mono.empty();
    }

    private Mono<Void> callKeycloakLogoutApi(String idToken) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(8080)
                        .path("/realms/myrealm/protocol/openid-connect/logout")
                        .queryParam("id_token_hint", idToken)
                        .build())
                .retrieve()
                .bodyToMono(Void.class);
    }

}
