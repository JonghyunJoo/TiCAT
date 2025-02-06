package com.example.concertservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

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
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "콘서트 날짜", example = "2025-01-30")
    private LocalDate date;

    @NotNull
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(description = "콘서트 시작 시간", example = "19:30:00")
    private LocalTime startTime;

    @Schema(description = "전체 좌석 수", example = "300")
    private Long totalSeats;

    @Schema(description = "예약 가능한 좌석 수", example = "120")
    private Long availableSeats;

    @Schema(description = "좌석 가격 정보", example = "{\"VIP\": 150, \"Regular\": 100}")
    private String seatPricing;
}
