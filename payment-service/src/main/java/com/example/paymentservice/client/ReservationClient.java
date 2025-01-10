package com.example.paymentservice.client;

import com.example.paymentservice.vo.ReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "reservation-service")
public interface ReservationClient {

    @GetMapping("/{reservationId}")
    ReservationResponse getReservation(@PathVariable Long reservationId);
}

