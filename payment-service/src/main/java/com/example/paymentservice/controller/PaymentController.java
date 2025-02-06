package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PaymentRequestDto;
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
    @PostMapping()
    public ResponseEntity<PaymentResponseDto> createPayment(
            @RequestBody PaymentRequestDto paymentRequestDto) {
        PaymentResponseDto response = paymentService.processPayment(
                paymentRequestDto.getUserId(),
                paymentRequestDto.getReservationGroupId());
        return ResponseEntity.ok(response);
    }
}

