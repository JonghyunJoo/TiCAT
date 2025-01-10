package com.example.paymentservice.event;

import lombok.Data;

@Data
public class PaymentSuccessEvent {
    private Long reservationId;
    private Long seatId;
    private Long userId;
    private Long amount;
}
