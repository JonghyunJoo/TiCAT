package com.example.seatservice.event;

import lombok.Data;

@Data
public class PaymentSuccessEvent {
    private Long reservationId;
    private Long seatId;
    private Long userId;
    private int amount;
}
