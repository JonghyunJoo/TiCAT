package com.example.paymentservice.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Long id;
    private Long userId;
    private Long seatId;
    private String reservationStatus;
    private Long price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
