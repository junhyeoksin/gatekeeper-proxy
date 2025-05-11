package com.gatekeeper.proxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
/**
ReactiveRedisConnectionFactory	Lettuce 기반 비동기 연결
ReactiveStringRedisTemplate	String 기반의 Key-Value 작업 지원
사용 방식	RedisService에서 주입받아 opsForValue().set/get/delete 사용
 */
@Configuration
public class RedisConfig {

    /**
     * Redis를 Reactive 방식으로 사용하기 위한 Template Bean 등록
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveStringRedisTemplate(factory);
    }
}
