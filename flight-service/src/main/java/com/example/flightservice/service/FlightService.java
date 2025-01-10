package com.example.flightservice.service;

import com.example.flightservice.dto.FlightResponseDto;
import com.example.flightservice.entity.FlightPage;

public interface FlightService {
    FlightPage<FlightResponseDto> getFlightsByConditions(String startDate,
                                                         String endDate,
                                                         String departureCode,
                                                         String destinationCode,
                                                         int page,
                                                         int size,
                                                         String orderBy,
                                                         String direction);
    FlightResponseDto getFlightById(Long id);
}
