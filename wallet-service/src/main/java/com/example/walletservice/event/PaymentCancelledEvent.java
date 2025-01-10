package com.example.walletservice.event;

import lombok.Data;

@Data
public class PaymentCancelledEvent {
    private Long reservationId;
    private Long seatId;
    private Long userId;
}