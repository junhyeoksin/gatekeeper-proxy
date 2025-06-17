package com.gatekeeper.proxy.security;

import com.gatekeeper.proxy.service.RedisService;
import com.gatekeeper.proxy.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisAuthenticationFilter implements WebFilter {

    private final RedisService redisService;

    // 건너뛸 경로들
    private static final List<String> WHITELIST = List.of(
            "/actuator/",              // Actuator
            "/swagger-ui",             // Swagger UI
            "/v3/api-docs",            // OpenAPI docs
            "/oauth2/authorization/",  // 로그인 시작
            "/auth-clear/" ,            // ← 캐시 삭제용 엔드포인트
            "/login/oauth2/code/"      // OAuth2 콜백
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1) 화이트리스트에 걸리면 무조건 통과
        for (String prefix : WHITELIST) {
            if (path.startsWith(prefix)) {
                return chain.filter(exchange);
            }
        }

        // 2) 이미 인증된 요청(콜백 후)을 건너뛰기
        return exchange.getPrincipal()
                .flatMap(principal -> {
                    if (principal instanceof Authentication auth && auth.isAuthenticated()) {
                        return chain.filter(exchange);
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // 3) 헤더 검사 & Redis 토큰 검증
                    String authHeader = exchange.getRequest()
                            .getHeaders()
                            .getFirst(HttpHeaders.AUTHORIZATION);
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        String userId;
                        try {
                            userId = JwtUtils.extractUserId(token);
                        } catch (ParseException e) {
                            log.warn("JWT 파싱 실패", e);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        String redisKey = "auth:token:" + userId;
                        return redisService.get(redisKey)
                                .flatMap(storedToken -> {
                                    if (token.equals(storedToken)) {
                                        return chain.filter(exchange);
                                    } else {
                                        log.warn("Redis에 저장된 토큰 불일치 또는 없음");
                                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                        return exchange.getResponse().setComplete();
                                    }
                                })
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.warn("Redis에서 사용자 토큰 조회 결과 없음");
                                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                    return exchange.getResponse().setComplete();
                                }));
                    }

                    // 4) 헤더 자체가 없으면 인증 시작을 위해 401 대신 Spring Security 진입점으로 넘기기
                    //    (401을 직접 내리면 OAuth2 로그인 리다이렉트가 안 됩니다)
                    return chain.filter(exchange);
                }));
    }
}

