package com.gatekeeper.proxy.controller;

import com.gatekeeper.proxy.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CacheController {

    private final RedisService redisService;

    @DeleteMapping("/auth-clear/{userId}")
    public Mono<Void> clearAuthCache(@PathVariable String userId) {
        log.info("Redis 캐시 삭제 API 호출: userId={}", userId);
        return redisService.deleteUserCache(userId);
    }
}
