package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentResponseDto;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto processPayment(Long userId, Long reservationGroupId);

    void cancelPayment(List<Long> reservationIdList, Long userId);
}
