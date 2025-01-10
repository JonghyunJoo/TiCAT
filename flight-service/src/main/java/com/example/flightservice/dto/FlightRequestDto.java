package com.example.flightservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
@Schema(description = "항공편 검색을 위한 요청 데이터를 표현하는 DTO")
public class FlightRequestDto {
    @NotNull
    @Schema(title = "시작 시간", description = "검색하고자하는 기간의 시작 시간", example = "2025-01-15T08:00:00")
    private String startDate;

    @NotNull
    @Schema(title = "끝 시간", description = "검색하고자하는 기간의 끝 시간", example = "2025-01-15T11:00:00")
    private String endDate;

    @NotEmpty
    @Schema(title = "출발지", description = "검색하고자하는 항공편의 출발지")
    private String departure;

    @NotEmpty
    @Schema(title = "도착지", description = "검색하고자하는 항공편의 도착지")
    private String destination;

    private int page = 1;
    private int size = 10;
    private String orderBy = "departureTime";
    private String orderDirection = "ASC";
}
