package com.example.walletservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionHistoryResponseDto {
    private Long id;
    private Long userId;
    private Double amount;
    private String transactionType;
    private LocalDateTime createdAt;
}

