package com.example.seatservice.service;

import com.example.seatservice.dto.SeatResponseDto;
import com.example.seatservice.entity.Seat;
import com.example.seatservice.entity.SeatStatus;
import com.example.seatservice.exception.CustomException;
import com.example.seatservice.exception.ErrorCode;
import com.example.seatservice.repository.SeatRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
    private final ModelMapper modelMapper;

    public List<SeatResponseDto> getSeatingChart(Long concertScheduleId) {
        try {
            List<Seat> seats = seatRepository.findAllByConcertScheduleId(concertScheduleId);
            if (seats.isEmpty()) {
                log.warn("No seats found for concertScheduleId: {}", concertScheduleId);
                throw new CustomException(ErrorCode.NOT_FOUND_SEAT);
            }

            List<SeatResponseDto> seatList = new ArrayList<>();
            for (Seat seat : seats) {
                seatList.add(modelMapper.map(seat, SeatResponseDto.class));
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
        return modelMapper.map(seat, SeatResponseDto.class);
    }

    // 좌석 예약 처리
    public void handleSeatLock(Long userId, Long seatId) {
        String lockKey = "seat-lock:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SEAT));

                if (seat.getSeatStatus() == SeatStatus.AVAILABLE) {
                    seat.setSeatStatus(SeatStatus.LOCKED);
                    seat.setUserId(userId);
                    seatRepository.save(seat);
                    log.info("Seat {} successfully locked by user {}", seatId, userId);
                } else if (seat.getSeatStatus() == SeatStatus.LOCKED) {
                    if (seat.getUserId().equals(userId)) {
                        seat.setSeatStatus(SeatStatus.AVAILABLE);
                        seat.setUserId(null);
                        seatRepository.save(seat);
                        log.info("Seat {} successfully unlocked by user {}", seatId, userId);
                    } else {
                        log.warn("Seat {} is reserved", seatId);
                        throw new CustomException(ErrorCode.UNAVAILABLE_SEAT);
                    }
                } else {
                    log.warn("Seat {} is not available or locked", seatId);
                    throw new CustomException(ErrorCode.ALREADY_RESERVED_SEAT);
                }
            } else {
                log.warn("Failed to acquire lock for seat {}", seatId);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        } catch (InterruptedException e) {
            log.error("Error while acquiring lock for seat {}: {}", seatId, e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public Long getAvailableSeats(Long concertScheduleId) {
        return seatRepository.countByConcertScheduleIdAndSeatStatus(
                concertScheduleId, SeatStatus.AVAILABLE);
    }

    public void cancelSeatLock(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SEAT));
        seat.setSeatStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);
    }
}

