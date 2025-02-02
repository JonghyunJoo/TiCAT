package com.example.seatservice.messagequeue;


import com.example.seatservice.event.ReservationCanceledEvent;
import com.example.seatservice.event.ReservationSuccessEvent;
import com.example.seatservice.service.SeatService;
import com.example.seatservice.entity.Seat;
import com.example.seatservice.entity.SeatStatus;
import com.example.seatservice.repository.SeatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReserveSeatConsumer {

    private final SeatRepository seatRepository;
    private final ObjectMapper objectMapper;
    private final SeatService seatService;

    @KafkaListener(topics = "reservation_success_topic", groupId = "seat-service")
    public void onReservationSuccess(String message) {
        try {
            // 메시지를 ReservationSuccessEvent 객체로 변환
            ReservationSuccessEvent event = objectMapper.readValue(message, ReservationSuccessEvent.class);

            // 각 Seat ID에 대해 상태 업데이트
            for (Long seatId : event.getSeatId()) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));

                seat.updateStatus(SeatStatus.RESERVED);
                seatRepository.save(seat);
            }

            log.info("Reservation success for user {}: seats {}", event.getUserId(), event.getSeatId());
        } catch (Exception e) {
            log.error("Error processing reservation success message: {}", e.getMessage(), e);
        }
    }


    @KafkaListener(topics = "reservation_canceled_topic", groupId = "reservation-service")
    public void onReservationCanceled(String message) {
        try {
            ReservationCanceledEvent event = objectMapper.readValue(message, ReservationCanceledEvent.class);

            // 좌석 목록에 대해 반복 처리
            for (Long seatId : event.getSeatList()) {
                seatService.cancelSeatLock(seatId);
                log.info("Seat lock canceled for seat ID: {}", seatId);
            }

            log.info("Reservation canceled for seats: {}", event.getSeatList());
        } catch (Exception e) {
            log.error("Error processing reservation canceled message: {}", e.getMessage(), e);
        }
    }
}

