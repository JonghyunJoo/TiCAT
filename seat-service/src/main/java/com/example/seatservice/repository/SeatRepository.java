package com.example.seatservice.repository;

import java.util.List;

import com.example.seatservice.entity.Seat;
import com.example.seatservice.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findAllByConcertScheduleId(Long concertScheduleId);

    Long countByConcertScheduleIdAndSeatStatus(Long concertScheduleId, SeatStatus seatStatus);
}