package com.example.reservationservice.dto;

import com.example.reservationservice.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDto{
    private Long id;
    private Long userId;
    private Long seatId;
    private ReservationStatus reservationStatus;
    private Long amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
