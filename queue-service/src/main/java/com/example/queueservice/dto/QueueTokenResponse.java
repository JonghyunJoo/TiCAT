package com.example.queueservice.dto;

import com.example.queueservice.vo.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QueueTokenResponse {
    private String userId;
    private String flightId;
    private String status;
    private QueueStatus queueStatus;
}

