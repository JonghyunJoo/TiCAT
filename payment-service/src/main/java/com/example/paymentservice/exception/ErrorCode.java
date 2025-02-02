package com.example.paymentservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    PAYMENT_NOT_FOUND("PAYMENT_NOT_FOUND", "결제를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_REQUEST("INVALID_PAYMENT_REQUEST", "유효하지 않은 결제 요청입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_STATUS("INVALID_PAYMENT_STATUS", "잘못된 상태입니다.", HttpStatus.CONFLICT),
    RESERVATION_SERVICE_UNAVAILABLE("RESERVATION_SERVICE_UNAVAILABLE", "예약 서비스가 정상 동작하지 않습니다", HttpStatus.SERVICE_UNAVAILABLE),
    WALLET_SERVICE_UNAVAILABLE("WALLET_SERVICE_UNAVAILABLE", "지갑 서비스가 정상 동작하지 않습니다", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "내부 서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String key;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String key, String message, HttpStatus status) {
        this.key = key;
        this.message = message;
        this.status = status;
    }
}

