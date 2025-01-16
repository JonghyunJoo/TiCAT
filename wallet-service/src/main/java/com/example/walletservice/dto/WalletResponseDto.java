package com.example.walletservice.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class WalletResponseDto {
    private Long userId;

    private Long balance;


}
