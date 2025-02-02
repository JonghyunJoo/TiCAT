package com.example.concertservice.controller;

import com.example.concertservice.dto.*;
import com.example.concertservice.service.ConcertScheduleService;
import com.example.concertservice.service.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/")
@Tag(name = "Concert Controller", description = "콘서트 관련 API를 제공합니다.")
public class ConcertController {

    private final ConcertService concertService;
    private final ConcertScheduleService concertScheduleService;

    @Operation(summary = "콘서트 생성", description = "새로운 콘서트를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "콘서트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/")
    public ResponseEntity<ConcertResponseDto> createConcert(@RequestBody ConcertRequestDto concertRequestDto) {
        ConcertResponseDto response = concertService.createConcert(concertRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "콘서트 검색", description = "입력 조건을 기반으로 콘서트를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "404", description = "조건에 맞는 콘서트가 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/")
    public ResponseEntity<ConcertPageDto<ConcertResponseDto>> getConcertsByConditions(
            @RequestBody ConcertSearchRequestDto concertRequestDto) {

        ConcertPageDto<ConcertResponseDto> concertPage = concertService.getConcertsByConditions(concertRequestDto);
        return ResponseEntity.ok(concertPage);
    }

    @Operation(summary = "콘서트 정보 조회", description = "ID를 기준으로 콘서트 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 콘서트 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{concertId}")
    public ResponseEntity<ConcertResponseDto> getConcert(@PathVariable Long concertId) {
        ConcertResponseDto concertResponseDto = concertService.getConcertById(concertId);
        return ResponseEntity.ok(concertResponseDto);
    }

    @Operation(summary = "콘서트 스케줄 생성", description = "콘서트의 스케줄을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "스케줄 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/concertSchedule")
    public ResponseEntity<ConcertScheduleResponseDto> createSchedule(@RequestBody ConcertScheduleRequestDto concertScheduleRequestDto) {
        ConcertScheduleResponseDto concertScheduleResponseDto = concertScheduleService.createConcertSchedule(concertScheduleRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(concertScheduleResponseDto);
    }

    @Operation(summary = "콘서트 스케줄 조회", description = "콘서트 ID를 기준으로 모든 스케줄을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스케줄 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 콘서트의 스케줄 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/concertSchedule/{concertId}")
    public ResponseEntity<List<ConcertScheduleResponseDto>> getSchedulesByConcertId(@PathVariable Long concertId) {
        return ResponseEntity.ok(concertScheduleService.getSchedulesByConcertId(concertId));
    }

    @Operation(summary = "콘서트 스케줄 단건 조회", description = "concertScheduleId를 기준으로 특정 콘서트 스케줄을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스케줄 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 스케줄 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/concertSchedule/detail/{concertScheduleId}")
    public ResponseEntity<ConcertScheduleResponseDto> getScheduleById(@PathVariable Long concertScheduleId) {
        ConcertScheduleResponseDto schedule = concertScheduleService.getScheduleById(concertScheduleId);
        return ResponseEntity.ok(schedule);
    }

    @Operation(summary = "콘서트 삭제", description = "concertId를 기준으로 콘서트를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "콘서트 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 콘서트 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{concertId}")
    public ResponseEntity<Void> deleteConcert(@PathVariable Long concertId) {
        concertService.deleteConcert(concertId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "콘서트 스케줄 삭제", description = "concertScheduleId를 기준으로 콘서트 스케줄을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "스케줄 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "해당 ID의 스케줄 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/concertSchedules/{concertScheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long concertScheduleId) {
        concertScheduleService.deleteSchedule(concertScheduleId);
        return ResponseEntity.noContent().build();
    }
}
