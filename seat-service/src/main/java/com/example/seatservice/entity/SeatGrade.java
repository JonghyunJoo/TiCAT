package com.example.seatservice.entity;

import lombok.Getter;

@Getter
public enum SeatGrade {
    VIP("VIP석"),
    R("R석"),
    S("S석"),
    A("A석");

    private final String description;

    SeatGrade(String description) {
        this.description = description;
    }
}
