package com.example.queueservice.service;

import com.example.queueservice.dto.QueueResponseDto;

public interface QueueService {
    QueueResponseDto addToQueue(Long userId, Long concertScheduleId);
    QueueResponseDto getQueueStatus(Long userId, Long concertScheduleId);
    void deleteTokens(Long userId);
}



