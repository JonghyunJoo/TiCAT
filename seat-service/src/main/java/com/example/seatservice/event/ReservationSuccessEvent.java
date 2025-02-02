package com.example.seatservice.event;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReservationSuccessEvent {
    private List<Long> seatId;
    private Long userId;
}
