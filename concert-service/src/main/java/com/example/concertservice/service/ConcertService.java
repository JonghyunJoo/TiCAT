package com.example.concertservice.service;

import com.example.concertservice.dto.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

public interface ConcertService {
    ConcertResponseDto createConcert(ConcertRequestDto dto);

    ConcertPageDto<ConcertResponseDto> getConcertsByConditions(
            String title,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            int page,
            int size,
            String orderBy,
            String orderDirection);

    ConcertResponseDto getConcertById(Long id);

    LocalDateTime startTimeParser(String searchStartDate);

    LocalDateTime endTimeParser(String searchEndDate);

    void deleteConcert(Long id);
}
