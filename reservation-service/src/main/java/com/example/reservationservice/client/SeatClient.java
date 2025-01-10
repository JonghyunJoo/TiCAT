package com.example.reservationservice.client;

import com.example.reservationservice.vo.SeatResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "seat-service")
public interface SeatClient {
    @GetMapping("/{seatId}")
    SeatResponse getSeatById(@PathVariable Long seatId);
}
