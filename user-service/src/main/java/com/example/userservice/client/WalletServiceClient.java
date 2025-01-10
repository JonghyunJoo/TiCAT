package com.example.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@FeignClient(name="wallet-service")
public interface WalletServiceClient {

    @GetMapping("/balance/{id}")
    Long getBalance(@PathVariable Long id);

}
