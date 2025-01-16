package com.example.walletservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionHistoryRequestDto {
    private int page;
    private int size;
}
