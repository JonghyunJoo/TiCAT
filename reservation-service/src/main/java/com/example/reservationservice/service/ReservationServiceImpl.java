package com.example.reservationservice.service;

import com.example.reservationservice.client.SeatClient;
import com.example.reservationservice.dto.ReservationGroupResponseDto;
import com.example.reservationservice.dto.ReservationResponseDto;
import com.example.reservationservice.entity.Reservation;
import com.example.reservationservice.entity.ReservationGroup;
import com.example.reservationservice.entity.ReservationStatus;
import com.example.reservationservice.event.ReservationCanceledEvent;
import com.example.reservationservice.event.ReservationSuccessEvent;
import com.example.reservationservice.exception.CustomException;
import com.example.reservationservice.exception.ErrorCode;
import com.example.reservationservice.messagequeue.ReservationEventProducer;
import com.example.reservationservice.repository.ReservationGroupRepository;
import com.example.reservationservice.repository.ReservationRepository;
import com.example.reservationservice.vo.SeatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationGroupRepository reservationGroupRepository;
    private final ReservationEventProducer reservationEventProducer;
    private final SeatClient seatClient;
    private final ModelMapper modelMapper;

    @Override
    public ReservationGroupResponseDto createReservation(List<Long> seats, Long userId) {
        ReservationGroup reservationGroup = ReservationGroup.builder()
                .userId(userId)
                .status(ReservationStatus.RESERVING)
                .createdAt(LocalDateTime.now())
                .build();

        reservationGroupRepository.save(reservationGroup);

        List<Reservation> reservations = seats.stream()
                .map(seatId -> {
                    SeatResponse seatResponse = seatClient.getSeatById(seatId);
                    return Reservation.builder()
                            .userId(userId)
                            .seatId(seatId)
                            .reservationStatus(ReservationStatus.RESERVING)
                            .price(seatResponse.getPrice())
                            .createdAt(LocalDateTime.now())
                            .reservationGroup(reservationGroup)
                            .build();
                })
                .collect(Collectors.toList());

        reservationRepository.saveAll(reservations);

        ReservationSuccessEvent successEvent = ReservationSuccessEvent.builder()
                .seatId(seats)
                .userId(userId)
                .build();

        reservationEventProducer.sendReservationSuccessEvent(successEvent);

        List<ReservationResponseDto> reservationList = reservations.stream()
                .map(reservation -> modelMapper.map(reservation, ReservationResponseDto.class))
                .toList();

        ReservationGroupResponseDto reservationGroupResponseDto
                = modelMapper.map(reservationGroup, ReservationGroupResponseDto.class);
        reservationGroupResponseDto.setReservations(reservationList);

        return reservationGroupResponseDto;
    }


    @Override
    public void completeReserve(Long reservationGroupId) {
        ReservationGroup reservationGroup = reservationGroupRepository.findById(reservationGroupId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_GROUP_NOT_FOUND));

        reservationGroup.confirmGroupReservation();
        reservationGroupRepository.save(reservationGroup);

        List<Reservation> reservations = reservationRepository.findAllByReservationGroupId(reservationGroupId);
        for (Reservation reservation : reservations) {
            reservation.confirmReservation();
        }

        reservationRepository.saveAll(reservations);
    }

    @Override
    public void cancelReservationGroup(Long userId, Long reservationGroupId) {
        ReservationGroup reservationGroup = reservationGroupRepository.findById(reservationGroupId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_GROUP_NOT_FOUND));

        reservationGroup.cancelGroupReservation();
        reservationGroupRepository.save(reservationGroup);

        List<Reservation> reservations = reservationRepository.findAllByReservationGroupId(reservationGroupId);
        List<Long> seatList = new ArrayList<>();
        long totalAmount = 0;

        for (Reservation reservation : reservations) {
            reservation.cancelReservation();
            seatList.add(reservation.getSeatId());
            totalAmount += reservation.getPrice();
        }

        reservationRepository.saveAll(reservations);

        ReservationCanceledEvent event = ReservationCanceledEvent.builder()
                .seatList(seatList)
                .userId(userId)
                .amount(totalAmount)
                .build();

        reservationEventProducer.sendReservationCanceledEvent(event);
    }

    @Override
    public void cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.cancelReservation();
        reservationRepository.save(reservation);

        ReservationCanceledEvent event = ReservationCanceledEvent.builder()
                .seatList(Collections.singletonList(reservation.getSeatId()))
                .userId(userId)
                .amount(reservation.getPrice())
                .build();

        reservationEventProducer.sendReservationCanceledEvent(event);
    }

    @Override
    public ReservationResponseDto getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        return modelMapper.map(reservation, ReservationResponseDto.class);
    }

    @Override
    public ReservationGroupResponseDto getReservationGroupById(Long reservationGroupId) {
        ReservationGroup reservationGroup = reservationGroupRepository.findById(reservationGroupId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_GROUP_NOT_FOUND));
        return modelMapper.map(reservationGroup, ReservationGroupResponseDto.class);
    }


    @Override
    public List<ReservationResponseDto> getReservationsByUserId(Long userId) {
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId);
        return reservations.stream()
                .map(reservation -> modelMapper.map(reservation, ReservationResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationGroupResponseDto> getReservationGroupsByUserId(Long userId) {
        List<ReservationGroup> reservationGroups = reservationGroupRepository.findAllByUserId(userId);
        return reservationGroups.stream()
                .map(reservationGroup -> modelMapper.map(reservationGroup, ReservationGroupResponseDto.class))
                .collect(Collectors.toList());
    }

}
