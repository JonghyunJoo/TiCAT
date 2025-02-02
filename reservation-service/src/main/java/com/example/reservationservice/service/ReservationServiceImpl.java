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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public ReservationGroupResponseDto createReservation(List<Long> seats, Long userId) {
        log.info("Creating reservation for user: {}, seats: {}", userId, seats);

        ReservationGroup reservationGroup = ReservationGroup.builder()
                .userId(userId)
                .status(ReservationStatus.RESERVING)
                .createdAt(LocalDateTime.now())
                .build();
        reservationGroupRepository.save(reservationGroup);

        List<Reservation> reservations = new ArrayList<>();
        for (Long seatId : seats) {
            try {
                SeatResponse seatResponse = seatClient.getSeatById(seatId);
                Reservation reservation = Reservation.builder()
                        .userId(userId)
                        .seatId(seatId)
                        .reservationStatus(ReservationStatus.RESERVING)
                        .price(seatResponse.getPrice())
                        .createdAt(LocalDateTime.now())
                        .reservationGroup(reservationGroup)
                        .build();
                reservations.add(reservation);
            } catch (Exception e) {
                log.error("Failed to fetch seat info for seatId: {}", seatId, e);
                throw new CustomException(ErrorCode.SEAT_NOT_FOUND);
            }
        }

        reservationRepository.saveAll(reservations);

        ReservationSuccessEvent successEvent = ReservationSuccessEvent.builder()
                .seatId(seats)
                .userId(userId)
                .build();

        reservationEventProducer.sendReservationSuccessEvent(successEvent);
        log.info("Sent ReservationSuccessEvent for user: {}, seats: {}", userId, seats);

        List<ReservationResponseDto> reservationList = reservations.stream()
                .map(reservation -> modelMapper.map(reservation, ReservationResponseDto.class))
                .toList();

        ReservationGroupResponseDto reservationGroupResponseDto = modelMapper.map(reservationGroup, ReservationGroupResponseDto.class);
        reservationGroupResponseDto.setReservations(reservationList);

        return reservationGroupResponseDto;
    }

    @Override
    @Transactional
    public void completeReserve(Long reservationGroupId) {
        log.info("Completing reservation group: {}", reservationGroupId);

        ReservationGroup reservationGroup = reservationGroupRepository.findById(reservationGroupId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_GROUP_NOT_FOUND));

        reservationGroup.confirmGroupReservation();
        reservationGroupRepository.save(reservationGroup);

        List<Reservation> reservations = reservationRepository.findAllByReservationGroupId(reservationGroupId);
        reservations.forEach(Reservation::confirmReservation);

        reservationRepository.saveAll(reservations);
        log.info("Completed reservation group: {}", reservationGroupId);
    }

    @Override
    @Transactional
    public void cancelReservationGroup(Long userId, Long reservationGroupId) {
        log.info("Canceling reservation group: {} for user: {}", reservationGroupId, userId);

        ReservationGroup reservationGroup = reservationGroupRepository.findById(reservationGroupId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_GROUP_NOT_FOUND));

        if (!reservationGroup.getUserId().equals(userId)) {
            log.warn("User {} is not authorized to cancel reservation group {}", userId, reservationGroupId);
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

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
        log.info("Sent ReservationCanceledEvent for user: {}, amount: {}", userId, totalAmount);
    }

    @Override
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        log.info("Canceling reservation: {} for user: {}", reservationId, userId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUserId().equals(userId)) {
            log.warn("User {} is not authorized to cancel reservation {}", userId, reservationId);
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        reservation.cancelReservation();
        reservationRepository.save(reservation);

        ReservationCanceledEvent event = ReservationCanceledEvent.builder()
                .seatList(Collections.singletonList(reservation.getSeatId()))
                .userId(userId)
                .amount(reservation.getPrice())
                .build();

        reservationEventProducer.sendReservationCanceledEvent(event);
        log.info("Sent ReservationCanceledEvent for user: {}, seatId: {}", userId, reservation.getSeatId());
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
    public List<ReservationGroupResponseDto> getReservationGroupsByUserId(Long userId) {
        List<ReservationGroup> reservationGroups = reservationGroupRepository.findAllByUserId(userId);
        return reservationGroups.stream()
                .map(reservationGroup -> modelMapper.map(reservationGroup, ReservationGroupResponseDto.class))
                .collect(Collectors.toList());
    }
}
