package com.example.reservationservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationGrouplRequestDto {
    private Long userId;
    private Long reservationGroupId;
}
