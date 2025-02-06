package com.example.walletservice.event;

import lombok.Data;

@Data
public class PaymentSuccessEvent {
    private Long userId;
    private Long amount;
}
