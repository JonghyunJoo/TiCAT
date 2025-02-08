package com.example.seatservice.dto;

import com.example.seatservice.entity.SeatGrade;
import com.example.seatservice.entity.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatSelectRequestDto {
	private Long seatId;
	private Long userId;
}
