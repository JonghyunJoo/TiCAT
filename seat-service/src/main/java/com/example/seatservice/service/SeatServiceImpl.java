package com.example.seatservice.service;

import com.example.seatservice.client.FlightClient;
import com.example.seatservice.dto.SeatResponseDto;
import com.example.seatservice.entity.Seat;
import com.example.seatservice.entity.SeatStatus;
import com.example.seatservice.exception.CustomException;
import com.example.seatservice.exception.ErrorCode;
import com.example.seatservice.repository.SeatRepository;
import com.example.seatservice.vo.FlightResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class SeatServiceImpl implements SeatService {
    private final RedissonClient redissonClient;
    private final SeatRepository seatRepository;
    private final FlightClient flightClient;
    private final ModelMapper modelMapper;

    @Cacheable(value = "seatCache", key = "#flightId")
    public List<SeatResponseDto> getSeatingChart(Long flightId) {
        try {
            if (!validateFlight(flightId)) {
                throw new CustomException(ErrorCode.NOT_FOUND_FLIGHT);
            }

            List<Seat> seats = seatRepository.findAllByFlightId(flightId);
            if (seats.isEmpty()) {
                log.warn("No seats found for flightId: {}", flightId);
                throw new CustomException(ErrorCode.NOT_FOUND_SEAT);
            }

            // ModelMapper를 사용하여 Seat -> SeatResponseDto 변환
            List<SeatResponseDto> seatList = new ArrayList<>();
            for (Seat seat : seats) {
                seatList.add(modelMapper.map(seat, SeatResponseDto.class));  // 변환
            }
            return seatList;

        } catch (CustomException ex) {
            log.error("CustomException occurred: {}", ex.getErrorCode().getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error occurred while fetching seating chart: {}", ex.getMessage());
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    public SeatResponseDto getSeatById(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SEAT));
        return modelMapper.map(seat, SeatResponseDto.class);  // 변환
    }

    public boolean validateFlight(Long flightId) {
        try {
            FlightResponse flight = flightClient.getFlight(flightId);
            return flight != null;
        } catch (Exception ex) {
            log.error("Error while validating flight with id {}: {}", flightId, ex.getMessage());
            return false;
        }
    }

    // 좌석 예약 처리
    public boolean handleSeatReservation(Long seatId) {
        // 락 키 설정
        String lockKey = "seat-lock:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락을 걸고 일정 시간동안 대기
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

                if (seat.getSeatStatus() == SeatStatus.AVAILABLE) {
                    seat.updateStatus(SeatStatus.LOCKED);
                    seatRepository.save(seat);
                    log.info("Seat {} successfully locked", seat.getId());
                } else {
                    log.warn("Seat {} is not available for locking", seat.getId());
                    throw new CustomException(ErrorCode.UNAVAILABLE_SEAT);
                }
            } else {
                log.warn("Failed to acquire lock for seat {}", seatId);
                throw new CustomException(ErrorCode.UNAVAILABLE_SEAT);
            }
        } catch (InterruptedException e) {
            log.error("Error while acquiring lock for seat {}: {}", seatId, e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            log.info("Seat locked check : {}", lock.isLocked());
        }
        return true;
    }

    public void extendLock(Long seatId, int additionalSeconds) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

        String lockKey = "seat-lock:" + seat.getId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 이미 락이 걸려 있으면 추가 시간만큼 연장
            if (lock.isLocked()) {
                lock.lock(1, TimeUnit.DAYS);
                log.info("Lock extended for seat {}", seatId);
            }
        } catch (Exception e) {
            log.error("Error extending lock for seat {}: {}", seatId, e.getMessage());
        }
    }

    public void cancelSeatReservation(Long seatId) {
        // 1. Redisson Lock 해제
        RLock lock = redissonClient.getLock("seat-lock:" + seatId);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();  // 락 해제
            System.out.println("락 해제 성공: " + seatId);
        }

        // 2. Seat 상태 변경 (Available로 변경)
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SEAT));
        seat.setSeatStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);
    }
}

