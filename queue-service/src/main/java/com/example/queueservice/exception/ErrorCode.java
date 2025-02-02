package com.example.queueservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    ALREADY_IN_QUEUE("ALREADY_IN_QUEUE", "사용자가 이미 대기열에 있습니다", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "이미 만료된 토큰입니다", HttpStatus.BAD_REQUEST),
    TOKEN_NOT_FOUND("TOKEN_NOT_FOUND", "토큰이 존재하지 않습니다", HttpStatus.NOT_FOUND),
    QUEUE_ERROR("QUEUE_ERROR", "큐 처리 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR),
    REDIS_ERROR("REDIS_ERROR", "Redis 처리 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR),
    ACTIVATE_ERROR("ACTIVATE_ERROR", "토큰 활성화 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR),
    EXPIRATION_ERROR("EXPIRATION_ERROR", "만료된 토큰 삭제 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String key;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String key, String message, HttpStatus status) {
        this.key = key;
        this.message = message;
        this.status = status;
    }
}

