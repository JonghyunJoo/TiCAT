package com.example.walletservice.service;

import com.example.walletservice.dto.TransactionHistoryResponseDto;
import com.example.walletservice.dto.WalletResponseDto;
import com.example.walletservice.entity.Wallet;

import java.util.List;

public interface WalletService {
    WalletResponseDto createWallet(Long userId);
    Wallet deductBalance(Long userId, Long amount);
    Wallet chargeWallet(Long userId, Long amount);
    Wallet refundBalance(Long userId, Long amount);
    Wallet getWallet(Long userId);
    List<TransactionHistoryResponseDto> getTransactionHistory(Long userId, int page, int size);
}
