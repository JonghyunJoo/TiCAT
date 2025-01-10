package com.example.seatservice.client;

import com.example.seatservice.vo.FlightResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "flight-service")
public interface FlightClient {
    @GetMapping("/{flightId}")
    FlightResponse getFlight(@PathVariable Long flightId);
}
