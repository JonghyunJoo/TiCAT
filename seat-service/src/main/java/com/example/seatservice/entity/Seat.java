package com.example.seatservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "seats", indexes ={
        @Index(name = "idx_concert_schedule_id", columnList = "concert_schedule_id"),
        @Index(name = "idx_status_cs_id", columnList = "seat_status, concert_schedule_id")})
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long concertScheduleId;
    private Long userId;
    private Long price;
    @Enumerated(EnumType.STRING)
    private SeatGrade seatGrade;
    @Enumerated(EnumType.STRING)
    private SeatStatus seatStatus;
    private int seatRow;
    private int seatColumn;
}
