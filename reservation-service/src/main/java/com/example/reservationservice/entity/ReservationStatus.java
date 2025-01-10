package com.example.reservationservice.entity;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    RESERVING("예약 진행 중"),
    RESERVED("예약 완료"),
    CANCELLED("취소");

    private final String message;

    ReservationStatus(String message) {
        this.message = message;
    }
}
