package com.example.reservationservice.controller;

import com.example.reservationservice.dto.ReservationResponseDto;
import com.example.reservationservice.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@Tag(name = "reservation-controller", description = "예약 서비스를 위한 API")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "예약 생성", description = "좌석 ID와 사용자 ID를 제공하여 새로운 예약을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request (유효하지 않은 요청 데이터)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/{seatId}")
    public ResponseEntity<ReservationResponseDto> createReservation(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "예약할 좌석 ID", required = true) @PathVariable Long seatId) {
        ReservationResponseDto responseDto = reservationService.createReservation(seatId, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "사용자 예약 조회", description = "사용자의 모든 예약을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (예약 정보 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/user")
    public ResponseEntity<List<ReservationResponseDto>> getUserReservations(
            @RequestHeader("X-User-Id") Long userId) {
        List<ReservationResponseDto> reservations = reservationService.getReservationsByUserId(userId);
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "예약 상세 조회", description = "예약 ID를 기반으로 예약 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (예약 정보 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponseDto> getReservation(
            @PathVariable Long reservationId) {
        ReservationResponseDto responseDto = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(responseDto);
    }
}
