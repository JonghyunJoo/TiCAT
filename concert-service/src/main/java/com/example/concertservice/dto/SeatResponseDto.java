package com.example.concertservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "좌석 응답 데이터를 표현하는 DTO")
public class SeatResponseDto {
    private Long availableSeats;
}
