package com.example.flightservice.config;

import com.example.flightservice.dto.FlightResponseDto;
import com.example.flightservice.entity.Flight;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(Flight.class, FlightResponseDto.class)
                .addMappings(mapper -> {
                    mapper.map(Flight::getDeparture, FlightResponseDto::setDeparture);
                    mapper.map(Flight::getDestination, FlightResponseDto::setDestination);
                });
        return modelMapper;
    }
}
