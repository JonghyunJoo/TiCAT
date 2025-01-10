package com.example.seatservice.entity;

import lombok.Getter;

@Getter
public enum SeatGrade {
    ECONOMY("Economy Class"),
    BUSINESS("Business Class"),
    FIRST("First Class");

    private final String description;

    SeatGrade(String description) {
        this.description = description;
    }
}
