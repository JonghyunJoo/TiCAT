//package com.example.reservationservice.service;
//
//import com.example.reservationservice.client.SeatClient;
//import com.example.reservationservice.dto.ReservationResponseDto;
//import com.example.reservationservice.entity.Reservation;
//import com.example.reservationservice.entity.ReservationStatus;
//import com.example.reservationservice.exception.CustomException;
//import com.example.reservationservice.exception.ErrorCode;
//import com.example.reservationservice.repository.ReservationRepository;
//import com.example.reservationservice.vo.SeatResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.modelmapper.ModelMapper;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ReservationServiceImplTest {
//
//    @Mock
//    private ReservationRepository reservationRepository;
//
//    @Mock
//    private SeatClient seatClient;
//
//    @Mock
//    private ModelMapper modelMapper;
//
//    @InjectMocks
//    private ReservationServiceImpl reservationService;
//
//    private Reservation reservation;
//
//    @BeforeEach
//    void setUp() {
//        reservation = Reservation.builder()
//                .id(1L)
//                .userId(1L)
//                .seatId(1L)
//                .reservationStatus(ReservationStatus.RESERVING)
//                .price(100L)
//                .createdAt(LocalDateTime.now())
//                .build();
//    }
//
//    /**
//     * 예약이 정상적으로 생성되는지 테스트.
//     *
//     * 주어진 좌석 ID와 사용자 ID로 예약을 생성하고, 생성된 예약의 응답 DTO를 반환하는지 확인.
//     */
//    @Test
//    void shouldCreateReservation_whenValidSeatIdAndUserId() {
//        // Given: 예약할 좌석 정보가 있는 경우
//        when(seatClient.getSeatById(1L)).thenReturn(SeatResponse.builder()
//                .id(1L)
//                .price(100L)
//                .build());
//        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
//        when(modelMapper.map(any(Reservation.class), eq(ReservationResponseDto.class)))
//                .thenReturn(ReservationResponseDto.builder()
//                        .id(1L)
//                        .userId(1L)
//                        .seatId(1L)
//                        .reservationStatus(ReservationStatus.RESERVING)
//                        .price(100L)
//                        .build());
//
//        // When: createReservation 메서드 호출
//        ReservationResponseDto result = reservationService.createReservation(1L, 1L);
//
//        // Then: 예약 정보가 담긴 응답 DTO를 반환해야 함
//        assertNotNull(result);
//        assertEquals(1L, result.getId());
//        assertEquals(ReservationStatus.RESERVING, result.getReservationStatus());
//    }
//
//    /**
//     * 예약 완료가 정상적으로 처리되는지 테스트.
//     *
//     * 예약 ID에 해당하는 예약을 찾아 상태를 '완료'로 변경하고 저장하는지 확인.
//     */
//    @Test
//    void shouldCompleteReservation_whenValidReservationId() {
//        // Given: 존재하는 예약 ID
//        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
//        when(reservationRepository.save(reservation)).thenReturn(reservation);
//
//        // When: completeReserve 메서드 호출
//        reservationService.completeReserve(1L);
//
//        // Then: 예약 상태가 '완료'로 변경되어야 함
//        assertEquals(ReservationStatus.RESERVED, reservation.getReservationStatus());
//    }
//
//    /**
//     * 예약 취소가 정상적으로 처리되는지 테스트.
//     *
//     * 예약 ID에 해당하는 예약을 찾아 상태를 '취소'로 변경하고 저장하는지 확인.
//     */
//    @Test
//    void shouldCancelReservation_whenValidReservationId() {
//        // Given: 존재하는 예약 ID
//        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
//        when(reservationRepository.save(reservation)).thenReturn(reservation);
//
//        // When: cancelReservation 메서드 호출
//        reservationService.cancelReservation(1L);
//
//        // Then: 예약 상태가 '취소'로 변경되어야 함
//        assertEquals(ReservationStatus.CANCELLED, reservation.getReservationStatus());
//    }
//
//    /**
//     * 존재하지 않는 예약 ID에 대해 예외가 발생하는지 테스트.
//     *
//     * 예약 ID가 존재하지 않으면 CustomException이 발생해야 하는지 확인.
//     */
//    @Test
//    void shouldThrowException_whenReservationNotFound() {
//        // Given: 존재하지 않는 예약 ID
//        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());
//
//        // When / Then: 예약이 없으면 예외가 발생해야 함
//        CustomException exception = assertThrows(CustomException.class,
//                () -> reservationService.completeReserve(1L));
//        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
//    }
//
//    /**
//     * 사용자 ID로 모든 예약을 조회하는 메서드에 대한 테스트.
//     *
//     * 사용자의 모든 예약 목록을 반환하는지 확인.
//     */
//    @Test
//    void shouldReturnAllReservationsByUserId_whenValidUserId() {
//        // Given: 사용자 ID로 예약 정보가 있는 경우
//        when(reservationRepository.findAllByUserId(1L)).thenReturn(List.of(reservation));
//        when(modelMapper.map(any(Reservation.class), eq(ReservationResponseDto.class)))
//                .thenReturn(ReservationResponseDto.builder()
//                        .id(1L)
//                        .userId(1L)
//                        .seatId(1L)
//                        .reservationStatus(ReservationStatus.RESERVING)
//                        .price(100L)
//                        .build());
//
//        // When: getReservationsByUserId 메서드 호출
//        List<ReservationResponseDto> result = reservationService.getReservationsByUserId(1L);
//
//        // Then: 예약 목록을 반환해야 함
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals(ReservationStatus.RESERVING, result.get(0).getReservationStatus());
//    }
//}
