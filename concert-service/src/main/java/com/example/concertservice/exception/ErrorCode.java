package com.example.concertservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    CONCERT_NOT_FOUND("CONCERT_NOT_FOUND", "공연 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONCERT_SCHEDULE_NOT_FOUND("CONCERT_SCHEDULE_NOT_FOUND", "공연 일정 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_REQUEST("INVALID_REQUEST", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR("DATABASE_ERROR", "데이터 베이스 오류 발생.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String key;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String key, String message, HttpStatus status) {
        this.key = key;
        this.message = message;
        this.status = status;
    }
}

