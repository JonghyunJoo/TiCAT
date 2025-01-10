package com.example.queueservice.dto;

import lombok.Data;

@Data
public class QueueRequest {
    private String userId;
    private String flightId;
}
