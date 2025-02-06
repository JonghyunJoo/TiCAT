package com.example.walletservice.event;

import lombok.Data;

import java.util.List;

@Data
public class ReservationCanceledEvent {
    private List<Long> seatList;
    private Long userId;
    private Long amount;
}