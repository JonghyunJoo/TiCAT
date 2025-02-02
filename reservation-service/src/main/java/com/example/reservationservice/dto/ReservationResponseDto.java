package com.example.reservationservice.dto;


import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponseDto{
    private Long id;
    private Long userId;
    private Long seatId;
    private String reservationStatus;
    private Long price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
