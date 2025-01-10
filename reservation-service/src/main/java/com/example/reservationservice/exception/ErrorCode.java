package com.example.reservationservice.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    ALREADY_RESERVED("이미 선택된 좌석입니다."),
    RESERVATION_NOT_FOUND("예약을 찾을 수 없습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
