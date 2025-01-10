package com.example.walletservice.entity;

public enum TransactionType {
    CHARGE,    // 잔액 충전
    PAYMENT,   // 결제
    REFUND     // 결제 취소 (환불)
}
