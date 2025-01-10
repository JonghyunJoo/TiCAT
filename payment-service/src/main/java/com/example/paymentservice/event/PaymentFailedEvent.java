package com.example.paymentservice.event;

import lombok.Data;

@Data
public class PaymentFailedEvent {
    private Long reservationId;
    private Long seatId;
    private Long userId;
}
