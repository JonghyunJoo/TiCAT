package com.example.seatservice.service;

import com.example.seatservice.dto.SeatRequestDto;
import com.example.seatservice.dto.SeatResponseDto;

import java.util.List;

public interface SeatService {
    List<SeatResponseDto> createSeats(List<SeatRequestDto> seatRequestDtoList);
    List<SeatResponseDto> getSeatingChart(Long flightId);

    SeatResponseDto getSeatById(Long seatId);

    void handleSeatLock(Long userId, Long seatId);

    Long getAvailableSeats(Long concertScheduleId);

    void cancelSeatLock(Long seatId);
}
