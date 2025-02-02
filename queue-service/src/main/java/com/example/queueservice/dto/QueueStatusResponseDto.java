package com.example.queueservice.dto;

import com.example.queueservice.vo.QueueStatus;
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