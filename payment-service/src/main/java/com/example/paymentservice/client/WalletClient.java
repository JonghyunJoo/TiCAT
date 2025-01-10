package com.example.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @GetMapping("/balance/{userId}")
    Long getBalance(@PathVariable Long userId);
}
