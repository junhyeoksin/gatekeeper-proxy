package com.gatekeeper.proxy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatekeeper.proxy.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken &&
            authentication.getPrincipal() instanceof OidcUser user) {

            String userId = user.getSubject();  // JWT의 sub
            String accessToken = user.getIdToken().getTokenValue();
            Instant expiresAt = user.getIdToken().getExpiresAt();

            long secondsToExpire = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
            Duration tokenTTL = Duration.ofSeconds(secondsToExpire);

            try {
                String profileJson = objectMapper.writeValueAsString(user.getClaims());

                log.info("로그인 성공 - Redis에 사용자 캐시 저장 userId: {}", userId);
                return Mono.when(
                        redisService.set("auth:token:" + userId, accessToken, tokenTTL),
                        redisService.set("auth:profile:" + userId, profileJson, Duration.ofMinutes(30))
                ).then();
            } catch (Exception e) {
                log.error("Redis 캐싱 중 오류 발생", e);
                return Mono.empty();
            }
        }

        return Mono.empty();
    }
}
