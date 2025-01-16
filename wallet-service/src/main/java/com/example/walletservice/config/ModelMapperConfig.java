package com.example.walletservice.config;

import com.example.walletservice.dto.TransactionHistoryResponseDto;
import com.example.walletservice.entity.TransactionHistory;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(TransactionHistory.class, TransactionHistoryResponseDto.class)
                .addMappings(mapper -> {
                    mapper.map(TransactionHistory::getTransactionType, TransactionHistoryResponseDto::setTransactionType);
                });
        return modelMapper;
    }
}
