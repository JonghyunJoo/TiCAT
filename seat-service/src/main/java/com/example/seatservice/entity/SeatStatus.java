package com.example.seatservice.entity;

import lombok.Getter;

@Getter
public enum SeatStatus {
    AVAILABLE("좌석을 예약할 수 있습니다"),
    LOCKED("다른 사용자가 선택 중입니다."),
    RESERVED("이미 예약된 좌석입니다.");

    private final String message;

    SeatStatus(String message) {
        this.message = message;
    }
}