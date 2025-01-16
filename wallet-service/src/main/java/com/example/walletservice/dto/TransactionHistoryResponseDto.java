package com.example.walletservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponseDto {
    private Long id;
    private Long userId;
    private Long amount;
    private String transactionType;
    private LocalDateTime createdAt;
    private Long balanceAfterTransaction;
}

