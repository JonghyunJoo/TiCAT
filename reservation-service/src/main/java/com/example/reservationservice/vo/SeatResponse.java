package com.example.reservationservice.vo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
public class SeatResponse {
    private Long id;
    private Long flightId;
    private Long price;
    private String seatGrade;
    private String seatStatus;
    private int number;
}
