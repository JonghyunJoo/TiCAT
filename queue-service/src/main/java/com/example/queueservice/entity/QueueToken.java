package com.example.queueservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueueToken {
    private String userId;
    private String flightId;
    private Long requestTime;
    private long expirationTime;
}
