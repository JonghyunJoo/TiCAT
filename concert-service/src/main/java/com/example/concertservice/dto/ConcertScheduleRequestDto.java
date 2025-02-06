package com.example.concertservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "콘서트 스케줄 요청 데이터를 표현하는 DTO")
public class ConcertScheduleRequestDto {
    @Schema(description = "콘서트 ID")
    private Long concertId;

    @Schema(description = "콘서트 일정 날짜", example = "2025-01-15")
    private LocalDate date;

    @Schema(description = "콘서트 시작 시간", example = "08:00:00")
    private LocalTime startTime;

    @Schema(description = "총 좌석 수")
    private Long totalSeats;

    @Schema(description = "좌석 가격")
    private Map<String, Long> seatPricing;
}
