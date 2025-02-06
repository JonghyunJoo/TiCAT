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
public class ReservationKafkaListener {

    private final SeatRepository seatRepository;
    private final ObjectMapper objectMapper;
    private final SeatService seatService;

    @KafkaListener(topics = "reservation_success_topic", groupId = "seat-service")
    public void onReservationSuccess(String message) {
        try {
            ReservationSuccessEvent event = objectMapper.readValue(message, ReservationSuccessEvent.class);

            for (Long seatId : event.getSeatIdList()) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));

                seat.setSeatStatus(SeatStatus.RESERVED);
                seatRepository.save(seat);
            }

            log.info("Reservation success for user {}: seats {}", event.getUserId(), event.getSeatIdList());
        } catch (Exception e) {
            log.error("Error processing reservation success message: {}", e.getMessage(), e);
        }
    }


    @KafkaListener(topics = "reservation_canceled_topic", groupId = "reservation-service")
    public void onReservationCanceled(String message) {
        try {
            ReservationCanceledEvent event = objectMapper.readValue(message, ReservationCanceledEvent.class);

            for (Long seatId : event.getSeatIdList()) {
                seatService.cancelSeatLock(seatId);
                log.info("Seat lock canceled for seat ID: {}", seatId);
            }

            log.info("Reservation canceled for seats: {}", event.getSeatIdList());
        } catch (Exception e) {
            log.error("Error processing reservation canceled message: {}", e.getMessage(), e);
        }
    }
}

