package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PaymentResponseDto;
import com.example.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "PaymentController", description = "결제 서비스를 위한 API")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 처리", description = "주어진 요청 데이터를 기반으로 결제를 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request (요청 데이터 오류)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/{reservationId}")
    public ResponseEntity<PaymentResponseDto> createPayment(
            @RequestHeader(value = "X-User-Id") Long userId,
            @PathVariable Long reservationId) {
        PaymentResponseDto response = paymentService.processPayment(reservationId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 취소", description = "결제 ID를 기반으로 결제를 취소")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found (결제가 존재하지 않음)"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<String> cancelPayment(
            @PathVariable Long reservationId) {
        paymentService.cancelPayment(reservationId);
        return ResponseEntity.ok("취소가 완료되었습니다.");
    }
}

