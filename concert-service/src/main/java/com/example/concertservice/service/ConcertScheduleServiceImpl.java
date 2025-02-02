package com.example.concertservice.service;

import com.example.concertservice.client.SeatClient;
import com.example.concertservice.dto.ConcertScheduleRequestDto;
import com.example.concertservice.dto.ConcertScheduleResponseDto;
import com.example.concertservice.entity.ConcertSchedule;
import com.example.concertservice.exception.CustomException;
import com.example.concertservice.exception.ErrorCode;
import com.example.concertservice.repository.ConcertScheduleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor

public class ConcertScheduleServiceImpl implements ConcertScheduleService {

    private final ConcertScheduleRepository concertScheduleRepository;
    private final ModelMapper modelMapper;
    private final SeatClient seatClient;

    @Override
    public ConcertScheduleResponseDto createConcertSchedule(ConcertScheduleRequestDto concertScheduleRequestDto) {
        ConcertSchedule concertSchedule = modelMapper.map(concertScheduleRequestDto, ConcertSchedule.class);

        ConcertSchedule savedConcertSchedule = concertScheduleRepository.save(concertSchedule);
        return modelMapper.map(savedConcertSchedule, ConcertScheduleResponseDto.class);
    }

    @Override
    public ConcertScheduleResponseDto getScheduleById(Long id) {
        ConcertSchedule concertSchedule = concertScheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND));

        Long availableSeats = seatClient.getAvailableSeatsForSchedules(id).getAvailableSeats();

        ConcertScheduleResponseDto responseDto = modelMapper.map(concertSchedule, ConcertScheduleResponseDto.class);
        responseDto.setAvailableSeats(availableSeats);

        return responseDto;
    }

    @Override
    @Cacheable(
            value = "concertScheduleCache",
            key = "#concertId",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<ConcertScheduleResponseDto> getSchedulesByConcertId(Long concertId) {
        List<ConcertSchedule> concertSchedules = concertScheduleRepository.findByConcertId(concertId);

        return concertSchedules.stream()
                .map(schedule -> {
                    ConcertScheduleResponseDto responseDto = modelMapper.map(schedule, ConcertScheduleResponseDto.class);
                    Long availableSeats = seatClient.getAvailableSeatsForSchedules(schedule.getId()).getAvailableSeats();
                    responseDto.setAvailableSeats(availableSeats);
                    return responseDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSchedule(Long id) {
        ConcertSchedule concertSchedule = concertScheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_SCHEDULE_NOT_FOUND));

        concertScheduleRepository.delete(concertSchedule);
    }
}
