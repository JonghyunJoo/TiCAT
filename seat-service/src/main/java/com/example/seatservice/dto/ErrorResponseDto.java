package com.example.seatservice.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponseDto {
    private final String message;
    private final String errorCode;
    private final int status;
    private final long timestamp;

    public static ErrorResponseDto of(String message, String errorCode, int status) {
        return ErrorResponseDto.builder()
                .message(message)
                .errorCode(errorCode)
                .status(status)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
