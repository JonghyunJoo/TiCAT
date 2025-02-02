package com.example.queueservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueueToken {
    private Long userId;
    private Long concertScheduleId;
    private Long requestTime;
}
