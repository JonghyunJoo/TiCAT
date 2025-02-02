package com.example.concertservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Convert;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "콘서트 스케줄 응답 데이터를 표현하는 DTO")
public class ConcertScheduleResponseDto {
    @Schema(description = "콘서트 스케줄 ID", example = "1001")
    private Long id;

    @NotNull
    @Schema(description = "콘서트 ID", example = "1")
    private Long concertId;

    @NotNull
    @Schema(description = "콘서트 날짜", example = "2025-01-30")
    private LocalDate date;

    @NotNull
    @Schema(description = "콘서트 시작 시간", example = "19:30:00")
    private LocalTime startTime;

    @Schema(description = "전체 좌석 수", example = "300")
    private Long totalSeats;

    @Schema(description = "예약 가능한 좌석 수", example = "120")
    private Long availableSeats;

    @Schema(description = "좌석 가격 정보", example = "{\"VIP\": 150, \"Regular\": 100}")
    private String seatPricing;
}
