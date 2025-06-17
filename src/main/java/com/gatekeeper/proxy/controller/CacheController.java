package com.gatekeeper.proxy.controller;

import com.gatekeeper.proxy.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CacheController {

    private final RedisService redisService;

    @DeleteMapping("/auth-clear/{userId}")
    public Mono<Void> clearAuthCache(@PathVariable String userId) {
        String tokenKey   = "auth:token:" + userId;
        String profileKey = "auth:profile:" + userId;
        System.out.println("tokenKey::" + tokenKey + "profileKey::" + profileKey);
        System.out.println("userId::" + userId );
        return Mono.when(
                redisService.delete(tokenKey),
                redisService.delete(profileKey)
        ).then();
    }
}
