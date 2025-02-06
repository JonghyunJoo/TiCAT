package com.example.walletservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    WALLET_ALREADY_EXISTS("WALLET_ALREADY_EXISTS", "이미 지갑이 존재합니다", HttpStatus.CONFLICT),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    WALLET_NOT_FOUND("WALLET_NOT_FOUND", "지갑을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TRANSACTION_NOT_FOUND("TRANSACTION_NOT_FOUND", "거래 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TRANSACTION_FAILED("TRANSACTION_FAILED", "거래에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TRANSACTION_TYPE("INVALID_TRANSACTION_TYPE", "유효하지 않은 거래 유형입니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED("PAYMENT_FAILED", "결제에 실패했습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_PROCESSED("PAYMENT_ALREADY_PROCESSED", "이미 처리된 결제입니다.", HttpStatus.CONFLICT),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    CONCURRENT_CHARGE_FAILED("CONCURRENT_CHARGE_FAILED","충전에 실패했습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("CONCURRENT_CHARGE_FAILED","내부 서버 오류" , HttpStatus.INTERNAL_SERVER_ERROR);

    private final String key;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String key, String message, HttpStatus status) {
        this.key = key;
        this.message = message;
        this.status = status;
    }
}
