package com.example.paymentservice.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("결제 대기 중"),
    COMPLETED("결제 완료"),
    CANCELLED("결제 취소");

    private final String message;

    PaymentStatus(String message) {
        this.message = message;
    }
}
