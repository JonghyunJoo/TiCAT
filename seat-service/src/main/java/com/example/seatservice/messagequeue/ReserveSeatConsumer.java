package com.example.seatservice.messagequeue;

import com.example.seatservice.event.PaymentCancelledEvent;
import com.example.seatservice.event.PaymentFailedEvent;
import com.example.seatservice.event.PaymentSuccessEvent;
import com.example.seatservice.service.SeatService;
import com.example.seatservice.vo.SeatReserveRequest;
import com.example.seatservice.entity.Seat;
import com.example.seatservice.entity.SeatStatus;
import com.example.seatservice.repository.SeatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReserveSeatConsumer {

    private final SeatRepository seatRepository;
    private final ObjectMapper objectMapper;
    private final SeatService seatService;

    @KafkaListener(topics = {"reserve_seat", "payment_success", "payment_failed", "payment_cancelled"}, groupId = "seat-service")
    public void consume(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            if ("reserve_seat".equals(topic)) {
                SeatReserveRequest request = objectMapper.readValue(message, SeatReserveRequest.class);
                seatService.handleSeatReservation(request.getSeatId());
            } else if ("payment_success_topic".equals(topic)) {
                PaymentSuccessEvent event = objectMapper.readValue(message, PaymentSuccessEvent.class);
                Seat seat = seatRepository.findById(event.getSeatId())
                        .orElseThrow(() -> new IllegalArgumentException("Seat not found"));
                seat.updateStatus(SeatStatus.RESERVED);
                seatRepository.save(seat);
            } else if ("payment_failed_topic".equals(topic)) {
                PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
                seatService.extendLock(event.getSeatId(), event.getLockExtensionSeconds());
            } else if ("payment_cancelled_topic".equals(topic)) {
                PaymentCancelledEvent event = objectMapper.readValue(message, PaymentCancelledEvent.class);
                seatService.cancelSeatReservation(event.getSeatId());
            }
        } catch (Exception e) {
            log.error("Error processing message from topic {}: {}", topic, e.getMessage());
        }
    }
}
