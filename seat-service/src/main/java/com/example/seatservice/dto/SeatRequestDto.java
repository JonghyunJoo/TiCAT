package com.example.seatservice.dto;

import com.example.seatservice.entity.SeatGrade;
import com.example.seatservice.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatRequestDto {
	private Long concertScheduleId;
	private Long price;
	private SeatGrade seatGrade;
	private SeatStatus seatStatus;
	private int rowNumber;
	private int columnNumber;
}
