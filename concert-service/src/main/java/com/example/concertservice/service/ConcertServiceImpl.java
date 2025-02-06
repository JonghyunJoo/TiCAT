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
public class ConcertServiceImpl implements ConcertService{
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
            key = "{#concertRequestDto.title, #concertRequestDto.searchStartDate, #concertRequestDto.searchEndDate}",
            unless = "#result == null || #result.content.isEmpty()"
    )
    public ConcertPageDto<ConcertResponseDto> getConcertsByConditions(ConcertSearchRequestDto concertRequestDto) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        try {
            Sort sort = Sort.by(concertRequestDto.getOrderBy());
            if (concertRequestDto.getOrderDirection().equalsIgnoreCase("DESC")) {
                sort = sort.descending();
            } else {
                sort = sort.ascending();
            }

            Pageable pageable = PageRequest.of(concertRequestDto.getPage(), concertRequestDto.getSize(), sort);

            Specification<Concert> spec = (root, query, criteriaBuilder) -> null;

            if (concertRequestDto.getTitle() != null) {
                spec = spec.and(ConcertSpecification.likeTitle(concertRequestDto.getTitle()));
            }


            if (concertRequestDto.getSearchStartDate() != null) {
                startDateTime = startTimeParser(concertRequestDto.getSearchStartDate());
                if (concertRequestDto.getSearchEndDate() != null) {
                    endDateTime = endTimeParser(concertRequestDto.getSearchEndDate());
                } else {
                    endDateTime = startDateTime.plusMonths(1);
                }
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

    private LocalDateTime startTimeParser(String startDate) {
        try {
            LocalDate parsedStartDate = LocalDate.parse(startDate);
            return parsedStartDate.atStartOfDay();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(startDate);
            } catch (DateTimeParseException ex) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }
    }

    private LocalDateTime endTimeParser(String endDate) {
        try {
            LocalDate parsedEndDate = LocalDate.parse(endDate);
            return parsedEndDate.atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(endDate);
            } catch (DateTimeParseException ex) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }
    }
}
