package com.example.paymentservice.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentFailedEvent {
    private Long reservationGroupId;
    private Long userId;
}
