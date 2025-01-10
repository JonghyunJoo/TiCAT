package com.example.walletservice.event;

import lombok.Data;

@Data
public class PaymentSuccessEvent {
    private Long reservationId;
    private Long seatId;
    private Long userId;
    private int amount;
}
