package com.example.reservationservice.client;

import com.example.reservationservice.vo.SeatResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "seat-service")
public interface SeatClient {
    @CircuitBreaker(name = "default-circuitbreaker", fallbackMethod = "getSeatByIdFallback")
    @GetMapping("/{seatId}")
    SeatResponse getSeatById(@PathVariable Long seatId);

    default SeatResponse getSeatByIdFallback(Long seatId, Throwable throwable) {
        return SeatResponse.builder()
                .seatStatus("불러오기 실패").build();
    }
}
