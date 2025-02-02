//package com.example.seatservice.service;
//
//import com.example.seatservice.client.FlightClient;
//import com.example.seatservice.dto.SeatResponseDto;
//import com.example.seatservice.entity.Seat;
//import com.example.seatservice.entity.SeatStatus;
//import com.example.seatservice.exception.CustomException;
//import com.example.seatservice.exception.ErrorCode;
//import com.example.seatservice.repository.SeatRepository;
//import com.example.seatservice.vo.FlightResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.modelmapper.ModelMapper;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class SeatServiceImplTest {
//
//    @Mock
//    private RedissonClient redissonClient;
//
//    @Mock
//    private SeatRepository seatRepository;
//
//    @Mock
//    private FlightClient flightClient;
//
//    @Mock
//    private ModelMapper modelMapper;
//
//    @InjectMocks
//    private SeatServiceImpl seatService;
//
//    private Seat seat;
//
//    @BeforeEach
//    void setUp() {
//        seat = Seat.builder()
//                .id(1L)
//                .flightId(100L)
//                .seatStatus(SeatStatus.AVAILABLE)
//                .build();
//    }
//
//    /**
//     좌석 정보가 존재할 때 올바른 좌석 목록을 반환하는지 테스트.
//     **/
//    @Test
//    void shouldReturnsSeatList_whenFlightExists() {
//        // Given: 특정 Flight ID에 대한 좌석 정보가 존재하는 경우
//        when(flightClient.getFlight(100L)).thenReturn(FlightResponse.builder().flightId(100L).build());
//        when(seatRepository.findAllByFlightId(100L)).thenReturn(List.of(seat));
//        when(modelMapper.map(any(Seat.class), eq(SeatResponseDto.class)))
//                .thenReturn(SeatResponseDto.builder()
//                        .id(1L)
//                        .flightId(100L)
//                        .seatStatus(SeatStatus.AVAILABLE).build());
//        // When: getSeatingChart 메서드 호출
//        var result = seatService.getSeatingChart(100L);
//
//        // Then: 좌석 정보를 반환해야 함
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(SeatStatus.AVAILABLE, result.get(0).getSeatStatus());
//    }
//
//    /**
//     좌석이 존재할 때 잠금을 성공적으로 처리하는지 테스트.
//     **/
//    @Test
//    void shouldSeatLocked_whenSeatExists() throws InterruptedException {
//        // Given: Redisson을 이용한 좌석 잠금 시도
//        RLock mockLock = mock(RLock.class);
//        when(redissonClient.getLock("seat-lock:1")).thenReturn(mockLock);
//        when(mockLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(true);
//        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
//
//        // When: handleSeatReservation 호출
//        seatService.handleSeatReservation(1L);
//
//        // Then: 좌석 상태가 LOCKED로 변경되고 저장되어야 함
//        assertEquals(SeatStatus.LOCKED, seat.getSeatStatus());
//        verify(seatRepository, times(1)).save(seat);
//    }
//
//    /**
//      예약 불가능한 좌석에 대해 예외를 발생시키는지 테스트.
//     **/
//    @Test
//    void shouldThrowsException_whenSeatNotAvailable() throws InterruptedException {
//        // Given: 이미 예약된 좌석
//        seat.setSeatStatus(SeatStatus.RESERVED);
//        RLock mockLock = mock(RLock.class);
//        when(redissonClient.getLock("seat-lock:1")).thenReturn(mockLock);
//        when(mockLock.tryLock(1, TimeUnit.SECONDS)).thenReturn(true);
//        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
//
//        // When / Then: 예외가 발생해야 함
//        CustomException exception = assertThrows(CustomException.class,
//                () -> seatService.handleSeatReservation(1L));
//        assertEquals(ErrorCode.UNAVAILABLE_SEAT, exception.getErrorCode());
//    }
//
//    /**
//      유효한 좌석 ID를 사용하여 좌석 잠금을 해제하는지 테스트.
//     **/
//    @Test
//    void shouldSeatUnlocked_whenValidSeatId() {
//        // Given: Redisson을 이용한 잠금 해제 시도
//        RLock mockLock = mock(RLock.class);
//        when(redissonClient.getLock("seat-lock:1")).thenReturn(mockLock);
//        when(mockLock.isLocked()).thenReturn(true);
//        when(mockLock.isHeldByCurrentThread()).thenReturn(true);
//        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
//
//        // When: cancelSeatReservation 호출
//        seatService.cancelSeatLock(1L);
//
//        // Then: 좌석 상태가 AVAILABLE로 변경되고 저장되어야 함
//        assertEquals(SeatStatus.AVAILABLE, seat.getSeatStatus());
//        verify(seatRepository, times(1)).save(seat);
//        verify(mockLock, times(1)).unlock();
//    }
//}
