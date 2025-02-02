package com.example.queueservice.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponseDto {
    private Long concertScheduleId;
    private String status;
    private long waitingOrder;
    private long remainingTime;
}