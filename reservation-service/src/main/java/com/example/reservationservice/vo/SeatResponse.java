package com.example.reservationservice.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatResponse {
    private Long id;
    private Long flightId;
    private Long price;
    private String seatGrade;
    private String seatStatus;
    private int number;
}
