package com.example.seatservice.service;

import com.example.seatservice.dto.SeatResponseDto;

import java.util.List;

public interface SeatService {
    List<SeatResponseDto> getSeatingChart(Long flightId);

    SeatResponseDto getSeatById(Long seatId);

    boolean validateFlight(Long flightId);

    boolean handleSeatReservation(Long seatId);

    void extendLock(Long seatId, int additionalSeconds);

    void cancelSeatReservation(Long seatId);
}
