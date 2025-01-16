package com.example.flightservice.service;

import com.example.flightservice.dto.FlightResponseDto;
import com.example.flightservice.entity.Airport;
import com.example.flightservice.entity.Flight;
import com.example.flightservice.entity.FlightPage;
import com.example.flightservice.exception.CustomException;
import com.example.flightservice.exception.ErrorCode;
import com.example.flightservice.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

    @InjectMocks
    private FlightServiceImpl flightService;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private ModelMapper modelMapper;

//    @Test
//    @DisplayName("조건에 따른 항공편 조회 - 성공")
//    void testGetFlightsByConditions_Success() {
//        // Given
//        String startDate = "2025-01-10";
//        String endDate = "2025-01-15";
//        String departureCode = "ICN";
//        String destinationCode = "JFK";
//        int page = 1;
//        int size = 10;
//        String orderBy = "departureTime";
//        String direction = "ASC";
//
//        LocalDateTime startDateTime = LocalDate.parse("2025-01-10").atStartOfDay();
//        LocalDateTime endDateTime = LocalDate.parse("2025-01-15").atTime(LocalTime.MAX);
//        Airport departure = Airport.ICN;
//        Airport destination = Airport.JFK;
//
//        Pageable pageable = PageRequest.of(0, size, Sort.by(orderBy).ascending());
//        Page<Flight> flightPage = new PageImpl<>(List.of(new Flight(1L, Airport.ICN, Airport.JFK,
//                LocalDateTime.of(2025, 1, 15, 8, 0), LocalDateTime.of(2025, 1, 15, 11, 0))));
//        when(flightRepository.findByDepartureTimeBetweenAndDepartureAndDestination(
//                any(LocalDateTime.class), any(LocalDateTime.class), any(Airport.class), any(Airport.class), any(Pageable.class)))
//                .thenReturn(flightPage);
//        when(modelMapper.map(any(Flight.class), eq(FlightResponseDto.class)))
//                .thenReturn(new FlightResponseDto(1L, departureCode, destinationCode, startDateTime, endDateTime));
//
//        // When
//        FlightPage<FlightResponseDto> result = flightService.getFlightsByConditions(
//                startDate, endDate, departureCode, destinationCode, page, size, orderBy, direction);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(1, result.getContent().size());
//        verify(flightRepository, times(1))
//                .findByDepartureTimeBetweenAndDepartureAndDestination(
//                        startDateTime, endDateTime, departure, destination, pageable);
//    }
//
//    @Test
//    @DisplayName("조건에 따른 항공편 조회 - 실패 (유효하지 않은 요청)")
//    void testGetFlightsByConditions_InvalidRequest() {
//        // Given
//        String startDate = "invalid-date";
//        String endDate = "2025-01-15";
//        String departureCode = "INVALID";
//        String destinationCode = "JFK";
//        int page = 1;
//        int size = 10;
//        String orderBy = "departureTime";
//        String direction = "ASC";
//
//        // When & Then
//        assertThrows(CustomException.class, () -> flightService.getFlightsByConditions(
//                startDate, endDate, departureCode, destinationCode, page, size, orderBy, direction));
//    }

    @Test
    @DisplayName("ID로 항공편 조회 - 성공")
    void testGetFlightById_Success() {
        // Given
        Long flightId = 1L;
        Flight flight = new Flight(flightId, Airport.ICN, Airport.JFK, LocalDateTime.now(), LocalDateTime.now().plusHours(14));
        FlightResponseDto flightResponseDto = new FlightResponseDto(flightId, "ICN", "JFK", flight.getDepartureTime(), flight.getArrivalTime());

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(flight));
        when(modelMapper.map(flight, FlightResponseDto.class)).thenReturn(flightResponseDto);

        // When
        FlightResponseDto result = flightService.getFlightById(flightId);

        // Then
        assertNotNull(result);
        assertEquals(flightId, result.getId());
        verify(flightRepository, times(1)).findById(flightId);
    }

    @Test
    @DisplayName("ID로 항공편 조회 - 실패 (항공편 없음)")
    void testGetFlightById_FlightNotFound() {
        // Given
        Long flightId = 999L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> flightService.getFlightById(flightId));
        assertEquals(ErrorCode.FLIGHT_NOT_FOUND, exception.getErrorCode());
        verify(flightRepository, times(1)).findById(flightId);
    }
}
