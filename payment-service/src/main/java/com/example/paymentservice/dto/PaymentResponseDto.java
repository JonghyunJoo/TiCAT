package com.example.paymentservice.dto;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDto {
    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "결제 금액", example = "100000")
    private Long amount;

    @Schema(description = "결제 상태", example = "COMPLETED")
    private String status;
}
