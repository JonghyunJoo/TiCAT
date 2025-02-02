package com.example.reservationservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    ALREADY_RESERVED("ALREADY_RESERVED","이미 선택된 좌석입니다.", HttpStatus.BAD_REQUEST),
    RESERVATION_NOT_FOUND("RESERVATION_NOT_FOUND","예약을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SEAT_NOT_FOUND("SEAT_NOT_FOUND","좌석을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS","잘못된 사용자의 요청입니다.", HttpStatus.UNAUTHORIZED),
    RESERVATION_GROUP_NOT_FOUND("RESERVATION_GROUP_NOT_FOUND", "예약 그룹을 찾을 수 없습니다", HttpStatus.NOT_FOUND);

    private final String key;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String key, String message, HttpStatus status) {
        this.key = key;
        this.message = message;
        this.status = status;
    }
}
