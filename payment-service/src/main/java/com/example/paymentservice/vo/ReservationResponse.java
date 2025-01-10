package com.example.paymentservice.vo;

import lombok.Data;

@Data
public class ReservationResponse {

    private Long reservationId; // 예약 ID
    private Long userId; // 사용자 ID
    private Long amount; // 결제 금액 (예: 가격)
    private Long seatId; // 좌석 ID

}
