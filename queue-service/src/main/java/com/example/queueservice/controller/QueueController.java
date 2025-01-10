package com.example.queueservice.controller;

import com.example.queueservice.dto.QueueRequest;
import com.example.queueservice.dto.QueueStatusResponse;
import com.example.queueservice.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "queue-controller", description = "대기열 관리를 위한 API")
public class QueueController {

    private final QueueService queueService;

    @Operation(summary = "대기열 추가", description = "사용자를 대기열에 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request (요청 데이터 오류)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping
    public ResponseEntity<QueueStatusResponse> createQueue(@RequestBody QueueRequest request) {
        QueueStatusResponse response = queueService.addToQueue(request.getUserId(), request.getFlightId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "대기열 상태 조회", description = "대기열 ID를 기반으로 상태를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (대기열이 존재하지 않음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping
    public ResponseEntity<QueueStatusResponse> getQueueStatus(@RequestBody QueueRequest request) {
        QueueStatusResponse response = queueService.getQueueStatus(request.getUserId(), request.getFlightId());
        return ResponseEntity.ok(response);
    }
}


