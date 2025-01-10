package com.example.reservationservice.event;

import lombok.Data;

@Data
public class PaymentSuccessEvent {
    private Long reservationId;
    private Long seatId;
    private Long userId;
    private int amount;
}
