package com.example.walletservice.messagequeue;

import com.example.walletservice.event.ReservationCanceledEvent;
import com.example.walletservice.service.WalletService;
import com.example.walletservice.vo.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaListener {

    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    // 결제 성공 시 처리
    @KafkaListener(topics = "payment_success_topic", groupId = "payment-service")
    public void onPaymentSuccess(String message) {
        try {
            PaymentResponse response = objectMapper.readValue(message, PaymentResponse.class);

            walletService.deductBalance(response.getUserId(), response.getAmount());

            log.info("Payment success for reservation {} and user {}", response.getReservationId(), response.getUserId());
        } catch (Exception e) {
            log.error("Error processing payment success message: {}", e.getMessage());
        }
    }

    // 결제 취소 시 처리
    @KafkaListener(topics = "Reservation_cancelled_topic", groupId = "payment-service")
    public void onPaymentCancelled(String message) {
        try {
            ReservationCanceledEvent reservationCanceledEvent = objectMapper.readValue(message, ReservationCanceledEvent.class);

            walletService.refundBalance(reservationCanceledEvent.getUserId(), reservationCanceledEvent.getAmount());

            log.info("Payment cancelled for user {}", reservationCanceledEvent.getUserId());
        } catch (Exception e) {
            log.error("Error processing payment cancelled message: {}", e.getMessage());
        }
    }
}
