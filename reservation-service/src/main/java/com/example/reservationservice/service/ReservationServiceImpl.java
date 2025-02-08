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
        log.info("Creating reservation group for user: {}", userId);

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
                long seatPrice = seatResponse.getPrice();
                Reservation reservation = Reservation.builder()
                        .userId(userId)
                        .seatId(seatId)
                        .reservationStatus(ReservationStatus.RESERVING)
                        .price(seatPrice)
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
                .seatIdList(seats)
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

        reservationGroup.setStatus(ReservationStatus.RESERVED);

        int updatedCount = reservationRepository.updateReservationStatusByGroupId(ReservationStatus.RESERVED, reservationGroupId);
        log.info("Updated {} reservations for group {}", updatedCount, reservationGroupId);
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

        reservationGroup.setStatus(ReservationStatus.CANCELLED);

        int updatedCount = reservationRepository.updateReservationStatusByGroupId(ReservationStatus.CANCELLED, reservationGroupId);
        log.info("Updated {} reservations to CANCELLED", updatedCount);

        List<Reservation> reservations = reservationRepository.findAllByReservationGroupId(reservationGroupId);
        List<Long> seatList = reservations.stream()
                .map(Reservation::getSeatId)
                .toList();

        List<Long> reservationIdList = reservations.stream()
                .map(Reservation::getId)
                .toList();

        ReservationCanceledEvent event = ReservationCanceledEvent.builder()
                .seatIdList(seatList)
                .userId(userId)
                .reservationIdList(reservationIdList)
                .build();

        reservationEventProducer.sendReservationCanceledEvent(event);
        log.info("Sent ReservationCanceledEvent for user: {}", userId);
    }

    @Override
    @Transactional
    public void cancelReservation(Long userId, List<Long> reservationIdList) {
        log.info("Canceling reservations: {} for user: {}", reservationIdList, userId);

        List<Reservation> reservations = reservationRepository.findAllById(reservationIdList);

        if (reservations.isEmpty()) {
            throw new CustomException(ErrorCode.RESERVATION_NOT_FOUND);
        }

        for (Reservation reservation : reservations) {
            if (!reservation.getUserId().equals(userId)) {
                log.warn("User {} is not authorized to cancel reservation {}", userId, reservation.getId());
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
            }
            reservation.setReservationStatus(ReservationStatus.CANCELLED);
        }

        List<Long> seatIdList = reservations.stream()
                .map(Reservation::getSeatId)
                .collect(Collectors.toList());

        ReservationCanceledEvent event = ReservationCanceledEvent.builder()
                .seatIdList(seatIdList)
                .userId(userId)
                .reservationIdList(reservationIdList)
                .build();

        reservationEventProducer.sendReservationCanceledEvent(event);
        log.info("Sent ReservationCanceledEvent for user: {}, seatIds: {}", userId, seatIdList);
    }

    public Long getTotalPriceByReservationGroupId(Long reservationGroupId) {
        return reservationRepository.findTotalPriceByReservationGroupId(reservationGroupId)
                .orElse(0L);
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
