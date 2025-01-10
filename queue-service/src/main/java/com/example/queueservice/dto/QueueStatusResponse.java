package com.example.queueservice.dto;

import com.example.queueservice.vo.QueueStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {
    private String flightId;
    private String status;
    private QueueStatus queueStatus;

    public QueueStatusResponse(String status) {
        this.status = status;
    }
}
