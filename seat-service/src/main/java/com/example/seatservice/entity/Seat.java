package com.example.seatservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "seats")
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
    private int rowNumber;
    private int columnNumber;


    public void updateStatus(SeatStatus status) {
        this.seatStatus = status;
    }

}
