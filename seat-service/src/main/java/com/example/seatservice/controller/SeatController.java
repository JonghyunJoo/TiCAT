package com.example.seatservice.controller;

import java.util.List;

import com.example.seatservice.dto.SeatCreateRequestDto;
import com.example.seatservice.dto.SeatResponseDto;
import com.example.seatservice.dto.SeatSelectRequestDto;
import com.example.seatservice.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@AllArgsConstructor
@Tag(name = "seat-controller", description = "좌석 관리를 위한 API")
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "좌석 생성", description = "좌석을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스케줄 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping()
    public ResponseEntity<List<SeatResponseDto>> saveSeats(@RequestBody List<SeatCreateRequestDto> seatCreateRequestDtoList) {
        List<SeatResponseDto> savedSeats = seatService.createSeats(seatCreateRequestDtoList);
        return ResponseEntity.ok(savedSeats);
    }

    @Operation(summary = "선택한 콘서트 스케쥴의 좌석 조회", description = "concertSchedule ID를 기준으로 좌석 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (콘서트 혹은 좌석 정보가 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("seats/{concertScheduleId}")
    public ResponseEntity<List<SeatResponseDto>> getSeatingChart(@PathVariable Long concertScheduleId) {
        List<SeatResponseDto> seats = seatService.getSeatingChart(concertScheduleId);
        return ResponseEntity.ok(seats);
    }

    @Operation(summary = "선택한 콘서트 스케쥴의 예약 가능한 좌석 갯수 조회", description = "concertSchedule ID를 기준으로 예약 가능 좌석 갯수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (콘서트 혹은 좌석 정보가 없음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping("availableSeats/{concertScheduleId}")
    public ResponseEntity<Long> getAvailableSeatsForSchedules(@PathVariable Long concertScheduleId) {
        Long availableSeatsCount = seatService.getAvailableSeats(concertScheduleId);
        return ResponseEntity.ok(availableSeatsCount);
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
    @PutMapping("/selectSeat")
    public ResponseEntity<Long> selectSeat(@RequestBody SeatSelectRequestDto seatSelectRequestDto) {
        seatService.handleSeatLock(seatSelectRequestDto.getUserId(), seatSelectRequestDto.getSeatId());
        return ResponseEntity.ok(seatSelectRequestDto.getSeatId());
    }
}
