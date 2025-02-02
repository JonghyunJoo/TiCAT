package com.example.concertservice.service;

import com.example.concertservice.dto.*;

public interface ConcertService {
    ConcertResponseDto createConcert(ConcertRequestDto dto);

    ConcertPageDto<ConcertResponseDto> getConcertsByConditions(ConcertSearchRequestDto concertRequestDto);

    ConcertResponseDto getConcertById(Long id);

    void deleteConcert(Long id);
}
