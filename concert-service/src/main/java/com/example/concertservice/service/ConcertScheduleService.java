package com.example.concertservice.service;

import com.example.concertservice.dto.ConcertScheduleRequestDto;
import com.example.concertservice.dto.ConcertScheduleResponseDto;
import com.example.concertservice.entity.ConcertSchedule;

import java.util.List;

public interface ConcertScheduleService {
    ConcertScheduleResponseDto createConcertSchedule(ConcertScheduleRequestDto dto);
    ConcertScheduleResponseDto getScheduleById(Long id);
    List<ConcertScheduleResponseDto> getSchedulesByConcertId(Long concertId);
    void deleteSchedule(Long id);
}
