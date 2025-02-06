package com.example.paymentservice.client;

import com.example.paymentservice.exception.CustomException;
import com.example.paymentservice.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @GetMapping("/balance/{userId}")
    @CircuitBreaker(name = "default-circuitbreaker", fallbackMethod = "getBalanceFallback")
    Long getBalance(@PathVariable Long userId);

    default Long getBalanceFallback(Long userId, Throwable throwable) {
        throw new CustomException(ErrorCode.WALLET_SERVICE_UNAVAILABLE);
    }
}
