package com.example.flightservice.service;

import com.example.flightservice.dto.FlightResponseDto;
import com.example.flightservice.entity.Airport;
import com.example.flightservice.entity.Flight;
import com.example.flightservice.entity.FlightPage;
import com.example.flightservice.exception.CustomException;
import com.example.flightservice.exception.ErrorCode;
import com.example.flightservice.repository.FlightRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
@Slf4j
@AllArgsConstructor
public class FlightServiceImpl implements FlightService {
    private final FlightRepository flightRepository;
    private final ModelMapper modelMapper;

    @Override
    @Cacheable(
            value = "flightCache",
            key = "{#startDate, #departureCode, #destinationCode, #page, #size, #orderBy, #direction}",
            unless = "#result.isEmpty()"
    )
    public FlightPage<FlightResponseDto> getFlightsByConditions(String startDate,
                                                                String endDate,
                                                                String departureCode,
                                                                String destinationCode,
                                                                int page,
                                                                int size,
                                                                String orderBy,
                                                                String direction) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        startDateTime = startTimeParser(startDate);
        endDateTime = endTimeParser(endDate);

        try {
            Sort sort = Sort.by(orderBy);
            if (direction.equalsIgnoreCase("DESC")) {
                sort = sort.descending();
            } else {
                sort = sort.ascending();
            }

            Pageable pageable = PageRequest.of(page - 1, size, sort);

            Airport departure = Airport.valueOf(departureCode);
            Airport destination = Airport.valueOf(destinationCode);

            return new FlightPage<>(flightRepository.findByDepartureTimeBetweenAndDepartureAndDestination(
                            startDateTime, endDateTime, departure, destination, pageable)
                    .map(flight -> modelMapper.map(flight, FlightResponseDto.class)));
        } catch (IllegalArgumentException ex) {
            log.error("항공편 검색 조건이 잘못되었습니다.", ex);
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        } catch (DataAccessException ex) {
            log.error("조건별 항공편 조회 중 데이터베이스 오류 발생", ex);
            throw new CustomException(ErrorCode.DATABASE_ERROR);
        }
    }

    @Override
    public FlightResponseDto getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FLIGHT_NOT_FOUND));

        return modelMapper.map(flight, FlightResponseDto.class);
    }

    private LocalDateTime startTimeParser(String startDate) {
        try {
            // 'YYYY-MM-DD' 형식의 날짜를 변환
            LocalDate parsedStartDate = LocalDate.parse(startDate);
            return parsedStartDate.atStartOfDay();
        } catch (DateTimeParseException e) {
            try {
                // 'YYYY-MM-DDTHH:MM:SS' 형식의 날짜를 변환
                return LocalDateTime.parse(startDate);
            } catch (DateTimeParseException ex) {
                // 예외를 CustomException으로 감싸서 던짐
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }
    }

    private LocalDateTime endTimeParser(String endDate) {
        try {
            // 'YYYY-MM-DD' 형식의 날짜를 변환
            LocalDate parsedEndDate = LocalDate.parse(endDate);
            return parsedEndDate.atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            try {
                // 'YYYY-MM-DDTHH:MM:SS' 형식의 날짜를 변환
                return LocalDateTime.parse(endDate);
            } catch (DateTimeParseException ex) {
                // 예외를 CustomException으로 감싸서 던짐
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        }
    }

}
