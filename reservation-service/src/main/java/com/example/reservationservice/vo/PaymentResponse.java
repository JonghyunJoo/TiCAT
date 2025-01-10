package com.example.reservationservice.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long reservationId;
    private Long userId;
    private Long amount;
    private String status; // 예: SUCCESS, FAILED, CANCELLED
    private String message; // 예: 결제 성공, 결제 실패 등의 메시지
}
