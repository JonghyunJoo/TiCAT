package com.example.seatservice.event;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReservationSuccessEvent {
    private List<Long> seatIdList;
    private Long userId;
}
