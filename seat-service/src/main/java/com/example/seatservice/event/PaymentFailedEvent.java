package com.example.seatservice.event;

import lombok.Data;

@Data
public class PaymentFailedEvent {
    private Long reservationId;
    private Long seatId;
    private Long userId;
    private int lockExtensionSeconds; // 락 연장 시간
}
