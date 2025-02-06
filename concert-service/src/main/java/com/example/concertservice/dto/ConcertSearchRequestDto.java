package com.example.concertservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "콘서트 검색 요청 데이터를 표현하는 DTO")
public class ConcertSearchRequestDto {
    @Schema(description = "콘서트 제목")
    private String title;

    @Schema(description = "검색 조건 시작일", example = "2025-01-15T08:00:00")
    private String searchStartDate;

    @Schema(description = "검색 조건 마감일", example = "2025-01-15T11:00:00")
    private String searchEndDate;

    private int page = 0;
    private int size = 10;
    private String orderBy = "departureTime";
    private String orderDirection = "ASC";
}
