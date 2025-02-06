package com.example.walletservice.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCanceledEvent {
    private Long userId;
    private Long amount;
}