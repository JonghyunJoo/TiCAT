package com.example.seatservice.dto;

import com.example.seatservice.entity.SeatGrade;
import com.example.seatservice.entity.SeatStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatResponseDto {
	private Long id;
	private Long concertScheduleId;
	private Long price;
	private SeatGrade seatGrade;
	private SeatStatus seatStatus;
	private int rowNumber;
	private int columnNumber;
}
