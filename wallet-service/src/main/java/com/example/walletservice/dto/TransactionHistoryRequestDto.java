package com.example.walletservice.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TransactionHistoryRequestDto {
    private int page;
    private int size;
}
