package com.gatekeeper.proxy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
/**
set(String key, String value, Duration ttl)	TTL을 설정해 Key-Value를 저장
get(String key)	저장된 값을 조회 (없으면 Mono.empty)
delete(String key)	해당 키를 삭제하고 성공 여부 반환
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Redis에 문자열 값 저장 (TTL 설정 가능)
     */
    public Mono<Boolean> set(String key, String value, Duration ttl) {
        return redisTemplate.opsForValue().set(key, value, ttl)
                .doOnNext(result -> System.out.println("🟢 Redis 저장 성공 여부: " + result + " key=" + key))
                .onErrorResume(e -> {
                    System.out.println("❌ Redis 저장 실패 key=" + key + " error=" + e.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * Redis에서 문자열 값 조회
     */
    public Mono<String> get(String key) {
        return redisTemplate.opsForValue()
                .get(key);
    }

    /**
     * Redis에서 키 삭제
     */
    public Mono<Boolean> delete(String key) {
        return redisTemplate.delete(key)
                .map(deletedCount -> deletedCount > 0)
                .doOnNext(deleted -> System.out.println("🔴 delete key='{}'? {}"+  key + " :: "  + deleted));
    }
}
