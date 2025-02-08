package com.example.concertservice.service;

import com.example.concertservice.dto.*;
import com.example.concertservice.entity.Concert;
import com.example.concertservice.exception.CustomException;
import com.example.concertservice.exception.ErrorCode;
import com.example.concertservice.repository.ConcertRepository;
import com.example.concertservice.repository.ConcertSpecification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
@Slf4j
@AllArgsConstructor
public class ConcertServiceImpl implements ConcertService {
    private final ConcertRepository concertRepository;
    private final ModelMapper modelMapper;

    @Override
    public ConcertResponseDto createConcert(ConcertRequestDto dto) {
        Concert concert = modelMapper.map(dto, Concert.class);

        Concert savedConcert = concertRepository.save(concert);

        return modelMapper.map(savedConcert, ConcertResponseDto.class);
    }

    @Override
    @Cacheable(
            value = "concertCache",
            key = "{#title ?: 'ALL', #startDateTime?: 'ALL', #endDateTime?: 'ALL'}",
            unless = "#result == null || #result.content.isEmpty()"
    )
    public ConcertPageDto<ConcertResponseDto> getConcertsByConditions(
            String title,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            int page,
            int size,
            String orderBy,
            String orderDirection) {

        try {
            Sort sort = Sort.by(orderBy);
            if (orderDirection.equalsIgnoreCase("DESC")) {
                sort = sort.descending();
            } else {
                sort = sort.ascending();
            }

            Pageable pageable = PageRequest.of(page, size, sort);

            Specification<Concert> spec = (root, query, criteriaBuilder) -> null;

            if (title != null) {
                spec = spec.and(ConcertSpecification.likeTitle(title));
            }
            if (startDateTime != null && endDateTime != null) {
                spec = spec.and(ConcertSpecification.betweenStartDateAndEndDate(startDateTime, endDateTime));
            }

            Page<Concert> concertPage = concertRepository.findAll(spec, pageable);
            return new ConcertPageDto<>(concertPage.map(concert -> modelMapper.map(concert, ConcertResponseDto.class)));
        } catch (IllegalArgumentException ex) {
            log.error("공연 검색 조건이 잘못되었습니다.", ex);
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        } catch (DataAccessException ex) {
            log.error("조건별 공연 조회 중 데이터베이스 오류 발생", ex);
            throw new CustomException(ErrorCode.DATABASE_ERROR);
        }
    }

    @Override
    public void deleteConcert(Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

        concertRepository.delete(concert);
    }

    @Override
    public ConcertResponseDto getConcertById(Long id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));

        return modelMapper.map(concert, ConcertResponseDto.class);
    }

    public LocalDateTime startTimeParser(String searchStartDate) {
        try {
            LocalDate parsedStartDate = LocalDate.parse(searchStartDate);
            return parsedStartDate.atStartOfDay();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(searchStartDate);
            } catch (DateTimeParseException ex) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }
    }

    public LocalDateTime endTimeParser(String searchEndDate) {
        try {
            LocalDate parsedEndDate = LocalDate.parse(searchEndDate);
            return parsedEndDate.atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(searchEndDate);
            } catch (DateTimeParseException ex) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }
    }
}
