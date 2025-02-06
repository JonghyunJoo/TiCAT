package com.example.reservationservice.event;

import lombok.Data;

@Data
public class PaymentFailedEvent {
    private Long reservationGroupId;
    private Long userId;
}
