package com.example.paymentservice.client;

import com.example.paymentservice.exception.CustomException;
import com.example.paymentservice.exception.ErrorCode;
import com.example.paymentservice.vo.ReservationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "reservation-service", url="http://reservation-service.reservation-ns.svc.cluster.local")
public interface ReservationClient {

    @GetMapping("/totalPrice/{reservationGroupId}")
    @CircuitBreaker(name = "default-circuitbreaker", fallbackMethod = "getReservationFallback")
    Long getTotalPrice(@PathVariable Long reservationGroupId);

    default Long getTotalPriceFallback(Long reservationId, Throwable throwable) {
        throw new CustomException(ErrorCode.RESERVATION_SERVICE_UNAVAILABLE);
    }

    @GetMapping("/{reservationId}")
    @CircuitBreaker(name = "default-circuitbreaker", fallbackMethod = "getReservationFallback")
    ReservationResponse getReservation(@PathVariable Long reservationId);

    default Long getReservationFallback(Long reservationId, Throwable throwable) {
        throw new CustomException(ErrorCode.RESERVATION_SERVICE_UNAVAILABLE);
    }
}

