package com.example.walletservice.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionHistoryRequestDto {
    private Long userId;
    private int page;
    private int size;
}
