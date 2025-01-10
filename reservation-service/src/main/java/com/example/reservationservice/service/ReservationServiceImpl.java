package com.example.reservationservice.service;

import com.example.reservationservice.client.SeatClient;
import com.example.reservationservice.dto.ReservationResponseDto;
import com.example.reservationservice.entity.Reservation;
import com.example.reservationservice.entity.ReservationStatus;
import com.example.reservationservice.exception.CustomException;
import com.example.reservationservice.exception.ErrorCode;
import com.example.reservationservice.repository.ReservationRepository;
import com.example.reservationservice.vo.SeatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatClient seatClient;
    private final ModelMapper modelMapper;

    @Override
    public ReservationResponseDto createReservation(Long seatId, Long userId) {
        SeatResponse seatResponse = seatClient.getSeatById(seatId);

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .seatId(seatId)
                .reservationStatus(ReservationStatus.RESERVING)
                .amount(seatResponse.getPrice())
                .createdAt(LocalDateTime.now())
                .build();

        reservation = reservationRepository.save(reservation);

        return modelMapper.map(reservation, ReservationResponseDto.class);
    }

    @Override
    public void completeReserve(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.confirmReservation();
        reservation = reservationRepository.save(reservation);
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.cancelReservation();
        reservation = reservationRepository.save(reservation);

    }

    @Override
    public ReservationResponseDto getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        return modelMapper.map(reservation, ReservationResponseDto.class);
    }

    @Override
    public List<ReservationResponseDto> getReservationsByUserId(Long userId) {
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId);
        return reservations.stream()
                .map(reservation -> modelMapper.map(reservation, ReservationResponseDto.class))
                .collect(Collectors.toList());
    }
}
