package com.example.seatservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long flightId;
    private Long price;
    @Enumerated(EnumType.STRING)
    private SeatGrade seatGrade;
    @Enumerated(EnumType.STRING)
    private SeatStatus seatStatus;
    private int number;


    public void updateStatus(SeatStatus status) {
        this.seatStatus = status;
    }

}
