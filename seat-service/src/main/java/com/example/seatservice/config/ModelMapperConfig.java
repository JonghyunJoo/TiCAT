package com.example.seatservice.config;

import com.example.seatservice.dto.SeatResponseDto;
import com.example.seatservice.entity.Seat;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(Seat.class, SeatResponseDto.class)
                .addMappings(mapper -> {
                    mapper.map(Seat::getSeatGrade, SeatResponseDto::setSeatGrade);
                    mapper.map(Seat::getSeatStatus, SeatResponseDto::setSeatStatus);
                });
        return modelMapper;
    }
}
