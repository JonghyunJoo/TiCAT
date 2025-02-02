package com.example.reservationservice.controller;

import com.example.reservationservice.dto.ReservationGroupResponseDto;
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
    @PostMapping()
    public ResponseEntity<ReservationGroupResponseDto> createReservation(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "예약할 좌석 ID", required = true) @RequestBody List<Long> seatList) {
        ReservationGroupResponseDto groupResponseDto = reservationService.createReservation(seatList, userId);
        return ResponseEntity.ok(groupResponseDto);
    }

    @Operation(summary = "사용자 예약 조회", description = "사용자의 모든 예약을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (예약 정보 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/user")
    public ResponseEntity<List<ReservationGroupResponseDto>> getUserReservationGroups(
            @RequestHeader("X-User-Id") Long userId) {
        List<ReservationGroupResponseDto> reservationGroupList = reservationService.getReservationGroupsByUserId(userId);
        return ResponseEntity.ok(reservationGroupList);
    }

    @Operation(summary = "예약 상세 조회", description = "예약 ID를 기반으로 예약 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (예약 정보 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/{reservationGroupId}")
    public ResponseEntity<ReservationGroupResponseDto> getReservationGroup(
            @PathVariable Long reservationGroupId) {
        ReservationGroupResponseDto groupResponseDto = reservationService.getReservationGroupById(reservationGroupId);
        return ResponseEntity.ok(groupResponseDto);
    }

    @Operation(summary = "예약 취소", description = "특정 예약 ID를 사용하여 예약을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "예약 취소 성공"),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @PostMapping("/cancelReservation/{reservationId}")
    public ResponseEntity<Void> cancelReservation(
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "취소할 예약 ID", required = true) @PathVariable Long reservationId) {
        reservationService.cancelReservation(userId, reservationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "예약 취소", description = "특정 예약 ID를 사용하여 예약을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "예약 취소 성공"),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @PostMapping("/cancelReservationGroup/{reservationGroupId}")
    public ResponseEntity<Void> cancelReservationGroup(
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "취소할 예약 ID", required = true) @PathVariable Long reservationGroupId) {
        reservationService.cancelReservationGroup(userId, reservationGroupId);
        return ResponseEntity.noContent().build();
    }
}
