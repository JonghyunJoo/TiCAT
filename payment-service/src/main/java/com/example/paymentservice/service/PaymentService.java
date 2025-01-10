package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto processPayment(Long reservationId, Long userId);

    void cancelPayment(Long paymentId);
}
