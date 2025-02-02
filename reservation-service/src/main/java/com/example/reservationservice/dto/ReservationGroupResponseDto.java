package com.example.reservationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationGroupResponseDto {
    private Long id;
    private Long userId;
    private String status;
    private LocalDateTime createdAt;
    private List<ReservationResponseDto> reservations;
}
