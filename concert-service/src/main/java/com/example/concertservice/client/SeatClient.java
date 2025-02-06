package com.example.concertservice.client;

import com.example.concertservice.dto.SeatResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "seat-service")
public interface SeatClient {
    @GetMapping("availableSeats/{concertScheduleId}")
    @CircuitBreaker(name = "default-circuitbreaker", fallbackMethod = "getAvailableSeatsForSchedulesFallback")
    SeatResponseDto getAvailableSeatsForSchedules(@PathVariable Long concertScheduleId);

    default SeatResponseDto getAvailableSeatsForSchedulesFallback(Long scheduleId, Throwable throwable) {
        return new SeatResponseDto(0L);
    }
}
