package com.example.concertservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "콘서트 요청 데이터를 표현하는 DTO")
public class ConcertRequestDto {
    @Schema(description = "콘서트 제목")
    private String title;

    @Schema(description = "공연장 정보")
    private String stage;

    @Schema(description = "공연 일정 시작일", example = "2025-01-15T08:00:00")
    private LocalDateTime concertStartDate;

    @Schema(description = "공연 일정 마감일", example = "2025-01-15T11:00:00")
    private LocalDateTime concertEndDate;
}