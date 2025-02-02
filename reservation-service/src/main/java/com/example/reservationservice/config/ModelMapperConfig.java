package com.example.reservationservice.config;

import com.example.reservationservice.dto.ReservationResponseDto;
import com.example.reservationservice.entity.Reservation;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(Reservation.class, ReservationResponseDto.class)
                .addMappings(mapper -> {
                    mapper.map(Reservation::getReservationStatus, ReservationResponseDto::setReservationStatus);

                });
        return modelMapper;
    }
}
