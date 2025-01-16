package com.example.flightservice.entity;

import lombok.Getter;

@Getter
public enum Airport {
    JFK("John F. Kennedy International Airport"),
    ICN("Incheon International Airport"),
    LHR("Heathrow Airport"),
    FRA("Frankfurt Airport"),
    HND("Tokyo Haneda Airport");

    private final String fullName;

    Airport(String fullName) {
        this.fullName = fullName;
    }
}

