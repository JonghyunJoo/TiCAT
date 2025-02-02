package com.example.reservationservice.event;

import lombok.Data;

@Data
public class PaymentSuccessEvent {
    private Long reservationGroupId;
    private Long userId;
    private int amount;
}
