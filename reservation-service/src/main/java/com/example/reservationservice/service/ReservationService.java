package com.example.reservationservice.service;

import com.example.reservationservice.dto.ReservationGroupResponseDto;
import com.example.reservationservice.dto.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    ReservationGroupResponseDto createReservation(List<Long> seats, Long userId);
    void  completeReserve(Long reservationId);
    void  cancelReservation(Long userId, List<Long> reservationIdList);
    void cancelReservationGroup(Long userId, Long reservationGroupId);
    Long getTotalPriceByReservationGroupId(Long reservationGroupId);
    ReservationResponseDto getReservationById(Long reservationId);
    ReservationGroupResponseDto getReservationGroupById(Long reservationGroupId);
    List<ReservationGroupResponseDto> getReservationGroupsByUserId(Long userId);
}
