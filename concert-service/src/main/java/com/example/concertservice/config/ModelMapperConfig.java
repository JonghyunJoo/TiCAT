package com.example.concertservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.modelmapper.ModelMapper;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.addConverter(new Converter<Map<String, Long>, String>() {
            @Override
            public String convert(MappingContext<Map<String, Long>, String> context) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.writeValueAsString(context.getSource());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to convert Map to JSON", e);
                }
            }
        });

        modelMapper.addConverter(new Converter<String, Map<String, Long>>() {
            @Override
            public Map<String, Long> convert(MappingContext<String, Map<String, Long>> context) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.readValue(context.getSource(), new TypeReference<Map<String, Long>>() {});
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to convert JSON to Map", e);
                }
            }
        });

        return modelMapper;
    }
}
