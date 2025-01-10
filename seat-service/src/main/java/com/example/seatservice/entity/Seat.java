package com.example.seatservice.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long flightId;
    private Long price;
    @Enumerated(EnumType.STRING)
    private SeatGrade seatGrade;
    @Setter
    @Enumerated(EnumType.STRING)
    private SeatStatus seatStatus;
    private int number;


    public void updateStatus(SeatStatus status) {
        this.seatStatus = status;
    }

}
