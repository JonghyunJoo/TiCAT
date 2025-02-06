package com.example.seatservice.event;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReservationCanceledEvent {
    private List<Long> seatIdList;
    private Long userId;
    private List<Long> reservationIdList;
}

