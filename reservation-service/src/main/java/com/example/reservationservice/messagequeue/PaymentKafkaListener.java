package com.example.reservationservice.messagequeue;

import com.example.reservationservice.event.PaymentFailedEvent;
import com.example.reservationservice.event.PaymentSuccessEvent;
import com.example.reservationservice.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaListener {

    private final ReservationService reservationService;
    private final ObjectMapper objectMapper;

    // 결제 성공 시 처리
    @KafkaListener(topics = "payment_success_topic", groupId = "payment-service")
    public void onPaymentSuccess(String message) {
        try {
            PaymentSuccessEvent event = objectMapper.readValue(message, PaymentSuccessEvent.class);

            reservationService.completeReserve(event.getReservationGroupId());

            log.info("Payment success for reservation {} and user {}", event.getReservationGroupId(), event.getUserId());
        } catch (Exception e) {
            log.error("Error processing payment success message: {}", e.getMessage());
        }
    }

    // 결제 취소 시 처리
    @KafkaListener(topics = "payment_failed_topic", groupId = "payment-service")
    public void onPaymentCancelled(String message) {
        try {
            PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);

            reservationService.cancelReservationGroup(event.getUserId(), event.getReservationGroupId());

            log.info("Payment cancelled for reservation {} and user {}", event.getReservationGroupId(), event.getUserId());
        } catch (Exception e) {
            log.error("Error processing payment cancelled message: {}", e.getMessage());
        }
    }
}
