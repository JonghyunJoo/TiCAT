package com.example.queueservice.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueueStatus {
    private long waitingOrder; // 대기 순번 (앞에 있는 사람의 수)
    private long remainingTime; // 대기 시간 (초 단위)
}
