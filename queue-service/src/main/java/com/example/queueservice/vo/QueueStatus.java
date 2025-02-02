package com.example.queueservice.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueueStatus {
    private long waitingOrder;
    private long remainingTime;
}
