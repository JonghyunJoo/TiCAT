package com.example.walletservice.service;

import com.example.walletservice.dto.TransactionHistoryResponseDto;
import com.example.walletservice.dto.WalletResponseDto;

import java.util.List;

public interface WalletService {
    WalletResponseDto createWallet(Long userId);
    void deductBalance(Long userId, Long amount);
    void chargeWallet(Long userId, Long amount);
    void refundBalance(Long userId, Long amount);
    Long getBalance(Long userId);
    List<TransactionHistoryResponseDto> getTransactionHistory(Long userId, int page, int size);
}
