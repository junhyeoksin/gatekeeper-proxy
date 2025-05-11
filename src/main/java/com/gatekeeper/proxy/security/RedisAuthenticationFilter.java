package com.gatekeeper.proxy.security;

import com.gatekeeper.proxy.service.RedisService;
import com.gatekeeper.proxy.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisAuthenticationFilter implements WebFilter {

    private final RedisService redisService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 1. Authorization 헤더 확인
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            String userId;
            try {
                // 2. JWT에서 userId(sub) 추출
                userId = JwtUtils.extractUserId(token);
            } catch (ParseException e) {
                log.warn("JWT 파싱 실패", e);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String redisKey = "auth:token:" + userId;

            // 3. Redis에 저장된 토큰과 비교
            return redisService.get(redisKey)
                    .flatMap(storedToken -> {
                        if (storedToken != null && storedToken.equals(token)) {
                            // ✅ 인증 통과
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

        // 4. Authorization 헤더 없음
        log.warn("Authorization 헤더 없음 또는 Bearer 형식 아님");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
