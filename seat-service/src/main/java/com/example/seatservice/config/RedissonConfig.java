package com.example.seatservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://redis-service.redis-ns.svc.cluster.local:6379")
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(5);
        return Redisson.create(config);
    }
}
