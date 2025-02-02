package com.example.userservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name="wallet-service")
public interface WalletServiceClient {

    @CircuitBreaker(name = "default-circuitbreaker", fallbackMethod = "getBalanceFallback")
    @GetMapping("/balance/{id}")
    Long getBalance(@PathVariable Long id);

    default Long getBalanceFallback(Long id, Throwable throwable) {
        return 0L;
    }
}
