package com.example.queueservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueRequestDto {
    Long userId;
    Long concertScheduleId;
}
