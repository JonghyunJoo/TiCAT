package com.example.flightservice.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "항공편 응답 데이터를 표현하는 DTO")
public class FlightResponseDto {
    @Schema( description = "항공편 ID", example = "1")
    private Long id;

    @Schema(description = "출발 공항 정보")
    private String departure;

    @Schema(description = "도착 공항 정보")
    private String destination;

    @Schema(description = "출발 시간", example = "2025-01-15T08:00:00")
    private LocalDateTime departureTime;

    @Schema(description = "도착 시간", example = "2025-01-15T11:00:00")
    private LocalDateTime arrivalTime;
}