package com.example.seatservice.event;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReservationCanceledEvent {
    private List<Long> seatList;
    private Long userId;
    private Long amount;
}