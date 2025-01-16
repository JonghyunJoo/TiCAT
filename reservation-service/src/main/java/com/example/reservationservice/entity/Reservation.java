package com.example.reservationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long seatId;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    private Long price;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void confirmReservation() {
        this.reservationStatus = ReservationStatus.RESERVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelReservation() {
        this.reservationStatus = ReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}
