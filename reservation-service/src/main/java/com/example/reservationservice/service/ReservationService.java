package com.example.reservationservice.service;

import com.example.reservationservice.dto.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    ReservationResponseDto createReservation(Long seatId, Long userId);
    void  completeReserve(Long reservationId);
    void  cancelReservation(Long reservationId);
    ReservationResponseDto getReservationById(Long reservationId);
    List<ReservationResponseDto> getReservationsByUserId(Long userId);
}
