package com.example.walletservice.service;

import com.example.walletservice.dto.TransactionHistoryResponseDto;
import com.example.walletservice.vo.ResponseWallet;

import java.util.List;

public interface WalletService {
    ResponseWallet createWallet(Long userId);
    void deductBalance(Long userId, Long amount);
    void chargeWallet(Long userId, Long amount);
    void refundBalance(Long userId, Long amount);
    Long getBalance(Long userId);
    List<TransactionHistoryResponseDto> getTransactionHistory(Long userId);
}
