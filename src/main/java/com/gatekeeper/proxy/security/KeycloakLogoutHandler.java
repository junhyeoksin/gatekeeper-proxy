package com.gatekeeper.proxy.security;

import com.gatekeeper.proxy.service.RedisService;
import lombok.RequiredArgsConstructor;
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

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakLogoutHandler implements ServerLogoutHandler {

    private final WebClient.Builder webClientBuilder;
    private final RedisService redisService;

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issuerUri;

    @Override
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token &&
                token.getPrincipal() instanceof OidcUser user) {

            String idToken = user.getIdToken().getTokenValue();
            String userId = user.getSubject();

            log.info("로그아웃 요청 시작: userId={}", userId);

            return callKeycloakLogoutApi(idToken)
                    .doOnSuccess(unused -> log.info("Keycloak 로그아웃 API 호출 성공"))
                    .doOnError(e -> log.error("Keycloak 로그아웃 API 호출 실패", e))
                    .onErrorResume(e -> Mono.empty())
                    .then(deleteRedisCache(userId))
                    .then(invalidateWebSession(exchange))
                    .doOnSuccess(unused -> log.info("전체 로그아웃 프로세스 완료"))
                    .doOnError(e -> log.error("전체 로그아웃 프로세스 실패", e));
        }
        return Mono.empty();
    }

    private Mono<Void> callKeycloakLogoutApi(String idToken) {
        URI logoutUri = URI.create(issuerUri + "/protocol/openid-connect/logout?id_token_hint=" + idToken);

        return webClientBuilder.build()
                .get()
                .uri(logoutUri)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Mono<Void> deleteRedisCache(String userId) {
        log.info("Redis 캐시 삭제 시작: userId={}", userId);
        return redisService.deleteUserCache(userId)
                .doOnSuccess(result -> log.info("Redis 캐시 삭제 성공: {}", userId))
                .doOnError(e -> log.error("Redis 캐시 삭제 실패", e));
    }

    private Mono<Void> invalidateWebSession(WebFilterExchange exchange) {
        return exchange.getExchange().getSession()
                .doOnNext(session -> log.info("Spring WebSession 무효화 시작"))
                .flatMap(WebSession::invalidate);
    }
}
