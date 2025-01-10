package com.example.queueservice.service;

import com.example.queueservice.dto.QueueStatusResponse;

public interface QueueService {
    QueueStatusResponse addToQueue(String userId, String flightId);
    QueueStatusResponse getQueueStatus(String userId, String flightId);
}



