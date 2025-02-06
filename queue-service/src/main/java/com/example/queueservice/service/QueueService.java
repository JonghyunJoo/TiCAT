package com.example.queueservice.service;

import com.example.queueservice.dto.QueueStatusResponseDto;

public interface QueueService {
    QueueStatusResponseDto addToQueue(Long userId, Long concertScheduleId);
    QueueStatusResponseDto getQueueStatus(Long userId, Long concertScheduleId);
    void deleteTokens(Long userId);
}



