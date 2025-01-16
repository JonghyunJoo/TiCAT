package com.example.flightservice.service;

import com.example.flightservice.dto.FlightPageDto;
import com.example.flightservice.dto.FlightRequestDto;
import com.example.flightservice.dto.FlightResponseDto;
import com.example.flightservice.entity.FlightPage;

public interface FlightService {
    FlightPageDto<FlightResponseDto> getFlightsByConditions(FlightRequestDto flightRequestDto);
    FlightResponseDto getFlightById(Long id);
}
