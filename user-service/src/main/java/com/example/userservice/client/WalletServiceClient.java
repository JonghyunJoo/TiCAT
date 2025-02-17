package com.example.userservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name="wallet-service")
public interface WalletServiceClient {
    @CircuitBreaker(name = "default-circuitbreaker", fallbackMethod = "createWalletFallback", url = "http://wallet-service.wallet-ns.svc.cluster.local")
    @PostMapping("/{userId}")
    Long createWallet(@PathVariable Long userId);

    default Long createWalletFallback(Long id, Throwable throwable) {
        return 0L;
    }


    @CircuitBreaker(name = "default-circuitbreaker", fallbackMethod = "getBalanceFallback")
    @GetMapping("/{userId}")
    Long getBalance(@PathVariable Long userId);

    default Long getBalanceFallback(Long userId, Throwable throwable) {
        return 0L;
    }
}
