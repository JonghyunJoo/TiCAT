package com.example.walletservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long amount;
    private String type;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private Long balanceAfterTransaction;
}

