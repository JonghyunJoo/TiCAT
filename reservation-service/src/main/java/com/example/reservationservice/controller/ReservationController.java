package com.example.reservationservice.controller;

import com.example.reservationservice.dto.*;
import com.example.reservationservice.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
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

            @RequestBody ReservationRequestDto reservationRequestDto) {
        ReservationGroupResponseDto groupResponseDto
                = reservationService.createReservation(
                        reservationRequestDto.getSeatIdList(),
                        reservationRequestDto.getUserId());
        return ResponseEntity.ok(groupResponseDto);
    }

    @Operation(summary = "사용자 예약 조회", description = "사용자의 모든 예약을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (예약 정보 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<List<ReservationGroupResponseDto>> getUserReservationGroups(
            @PathVariable Long userId) {
        List<ReservationGroupResponseDto> reservationGroupList = reservationService.getReservationGroupsByUserId(userId);
        return ResponseEntity.ok(reservationGroupList);
    }

    @Operation(summary = "예약 그룹 조회", description = "예약 그룹 ID를 기반으로 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (예약 정보 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/reservationGroups/{reservationGroupId}")
    public ResponseEntity<ReservationGroupResponseDto> getReservationGroup(
            @PathVariable Long reservationGroupId) {
        ReservationGroupResponseDto groupResponseDto = reservationService.getReservationGroupById(reservationGroupId);
        return ResponseEntity.ok(groupResponseDto);
    }

    @Operation(summary = "예약 조회", description = "예약 ID를 기반으로 예약 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (예약 정보 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<ReservationResponseDto> getReservationById(
            @PathVariable Long reservationId) {
        ReservationResponseDto reservationResponse = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(reservationResponse);
    }

    @Operation(summary = "예약 그룹 총액 조회", description = "예약 그룹 ID를 기반으로 예약 그룹의 총액을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (예약 그룹 정보 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/totalPrice/{reservationGroupId}")
    public ResponseEntity<Long> getTotalPrice(@PathVariable Long reservationGroupId) {
        Long totalPrice = reservationService.getTotalPriceByReservationGroupId(reservationGroupId);
        return ResponseEntity.ok(totalPrice);
    }

    @Operation(summary = "예약 취소", description = "특정 예약 ID를 사용하여 예약을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "예약 취소 성공"),
            @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @PutMapping("/reservations")
    public ResponseEntity<Void> cancelReservation(
            @RequestBody ReservationCancelRequestDto reservationCancelRequestDto) {
        reservationService.cancelReservation(reservationCancelRequestDto.getUserId(), reservationCancelRequestDto.getReservationIdList());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "예약 그룹 취소", description = "특정 예약 그룹 ID를 사용하여 예약을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "예약 그룹 취소 성공"),
            @ApiResponse(responseCode = "404", description = "예약 그룹을 찾을 수 없음")
    })
    @PutMapping("/reservationGroups")
    public ResponseEntity<Void> cancelReservationGroup(
            @RequestBody ReservationGrouplRequestDto reservationGrouplRequestDto) {
        reservationService.cancelReservationGroup(
                reservationGrouplRequestDto.getUserId(),
                reservationGrouplRequestDto.getReservationGroupId());
        return ResponseEntity.noContent().build();
    }
}
