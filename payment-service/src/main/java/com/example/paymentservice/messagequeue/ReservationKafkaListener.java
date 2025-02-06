package com.example.paymentservice.messagequeue;

import com.example.paymentservice.event.ReservationCanceledEvent;
import com.example.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationKafkaListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    // 예약 취소 시 처리
    @KafkaListener(topics = "reservation_canceled_topic", groupId = "reservation-service")
    public void onReservationCanceled(String message) {
        try {
            ReservationCanceledEvent reservationCanceledEvent = objectMapper.readValue(message, ReservationCanceledEvent.class);

            paymentService.cancelPayment(reservationCanceledEvent.getReservationIdList(), reservationCanceledEvent.getUserId());

            log.info("Reservation canceled : {}", reservationCanceledEvent.getReservationIdList());
        } catch (Exception e) {
            log.error("Error processing reservation canceled message: {}", e.getMessage(), e);
        }
    }

}
