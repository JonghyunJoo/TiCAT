package com.example.seatservice.dto;

import com.example.seatservice.entity.SeatGrade;
import com.example.seatservice.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatResponseDto {
	private Long id;
	private Long flightId;
	private Long price;
	private SeatGrade seatGrade;
	private SeatStatus seatStatus;
	private int number;
}
