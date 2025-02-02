package com.example.reservationservice.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatResponse {
    private Long id;
    private Long concertScheduleId;
    private Long price;
    private String seatGrade;
    private String seatStatus;
    private Long rowNumber;
    private Long columnNumber;
}
