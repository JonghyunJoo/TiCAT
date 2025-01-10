package com.example.seatservice.controller;

import java.util.List;

import com.example.seatservice.dto.SeatResponseDto;
import com.example.seatservice.exception.CustomException;
import com.example.seatservice.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@AllArgsConstructor
@Tag(name = "seat-controller", description = "좌석 관리를 위한 API")
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "선택한 항공편의 좌석 조회", description = "항공편 ID를 기준으로 좌석 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (항공편 또는 좌석 정보가 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("seats/{flightId}")
    public ResponseEntity<List<SeatResponseDto>> getSeatingChart(@PathVariable Long flightId) {
        List<SeatResponseDto> seats = seatService.getSeatingChart(flightId);
        return ResponseEntity.ok(seats);
    }

    @Operation(summary = "좌석 상세 조회", description = "좌석 ID를 기준으로 좌석 상세 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (좌석 정보가 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("/{seatId}")
    public ResponseEntity<SeatResponseDto> getSeatById(@PathVariable Long seatId) {
        SeatResponseDto seatResponse = seatService.getSeatById(seatId);
        return ResponseEntity.ok(seatResponse);
    }

    @Operation(summary = "좌석 선택 및 잠금", description = "좌석 ID를 제공하여 선택 및 잠금 작업을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK (좌석 잠금 성공)"),
            @ApiResponse(responseCode = "409", description = "Conflict (좌석 이미 잠금됨)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error (잠금 작업 실패)")
    })
    @PutMapping("/selectSeat/{seatId}")
    public ResponseEntity<String> selectSeat(@PathVariable Long seatId) {
        try {
            boolean isLocked = seatService.handleSeatReservation(seatId);

            if (isLocked) {
                return ResponseEntity.ok("Seat locked successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to lock seat.");
            }
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        }
    }
}
