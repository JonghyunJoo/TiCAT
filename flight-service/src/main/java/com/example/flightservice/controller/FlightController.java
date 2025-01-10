package com.example.flightservice.controller;

import com.example.flightservice.dto.FlightResponseDto;
import com.example.flightservice.entity.FlightPage;
import com.example.flightservice.service.FlightService;
import com.example.flightservice.dto.FlightRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/")
@Tag(name = "flight-controller", description = "항공편 서비스를 위한 API")
public class FlightController {
    private final FlightService flightService;

    @GetMapping("/search")
    @Operation(summary = "항공편 검색", description = "클라이언트로부터 입력받은 조건을 기반으로 항공편 검색")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (인증 실패 오류)"),
            @ApiResponse(responseCode = "403", description = "Forbidden (권한이 없는 페이지에 엑세스)"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND (검색된 항공편이 없을 경우)"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR"),
    })
    public ResponseEntity<FlightPage<FlightResponseDto>> getFlightsByConditions(
            @RequestBody FlightRequestDto flightRequestDto) {

            FlightPage<FlightResponseDto> flightsPage = flightService.getFlightsByConditions(
                    flightRequestDto.getStartDate(),
                    flightRequestDto.getEndDate(),
                    flightRequestDto.getDeparture(),
                    flightRequestDto.getDestination(),
                    flightRequestDto.getPage(),
                    flightRequestDto.getSize(),
                    flightRequestDto.getOrderBy(),
                    flightRequestDto.getOrderDirection());

            return ResponseEntity.ok(flightsPage);
    }

    @Operation(summary = "항공편 정보", description = "클라이언트로부터 입력받은 항공편 ID에 해당하는 항공편 정보를 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized (인증 실패 오류)"),
            @ApiResponse(responseCode = "403", description = "Forbidden (권한이 없는 페이지에 엑세스)"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND (항공편이 없을 경우)"),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR"),
    })
    @GetMapping("/{flightId}")
    public ResponseEntity<FlightResponseDto> getFlight(@PathVariable Long flightId) {
        FlightResponseDto flightResponseDto = flightService.getFlightById(flightId);
        return ResponseEntity.ok(flightResponseDto);
    }
}
