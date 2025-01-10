package com.example.walletservice.service;

import com.example.walletservice.dto.TransactionHistoryResponseDto;
import com.example.walletservice.entity.TransactionHistory;
import com.example.walletservice.entity.TransactionType;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.exception.CustomException;
import com.example.walletservice.exception.ErrorCode;
import com.example.walletservice.repository.TransactionHistoryRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.vo.ResponseWallet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private ModelMapper modelMapper;

    public ResponseWallet createWallet(Long userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new CustomException(ErrorCode.WALLET_ALREADY_EXISTS);
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(0L)
                .build();
        Wallet savedWallet = walletRepository.save(wallet);

        log.info("Wallet created for user {} with initial balance: {}", userId, savedWallet.getBalance());
        return modelMapper.map(savedWallet, ResponseWallet.class);
    }

    // 잔액 차감
    public void deductBalance(Long userId, Long amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

        // 잔액 차감
        if (wallet.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        wallet.setBalance(wallet.getBalance() - amount);

        walletRepository.save(wallet);

        // 거래 내역 저장 (결제 차감)
        saveTransactionHistory(userId, wallet, amount, TransactionType.PAYMENT);

        log.info("Deducted {} from user {} balance. New balance: {}", amount, userId, wallet.getBalance());
    }

    // 잔액 충전
    public void chargeWallet(Long userId, Long amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setBalance(wallet.getBalance() + amount); // 직접 엔티티 수정
        walletRepository.save(wallet);

        // 거래 내역 저장 (충전)
        saveTransactionHistory(userId, wallet, amount, TransactionType.CHARGE);

        log.info("Charged {} to user {} balance. New balance: {}", amount, userId, wallet.getBalance());
    }

    // 결제 취소 시 환불
    public void refundBalance(Long userId, Long amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        // 거래 내역 저장 (환불)
        saveTransactionHistory(userId, wallet, amount, TransactionType.REFUND);

        log.info("Refunded {} to user {} balance. New balance: {}", amount, userId, wallet.getBalance());
    }

    // 거래 내역 저장
    private void saveTransactionHistory(Long userId, Wallet wallet, Long amount, TransactionType transactionType) {
        TransactionHistory transactionHistory = TransactionHistory.builder()
                .amount(amount)
                .type(transactionType.name())
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .transactionType(transactionType)
                .balanceAfterTransaction(wallet.getBalance())
                .build();

        transactionHistoryRepository.save(transactionHistory);
    }

    // 잔액 조회
    public Long getBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
        return wallet.getBalance();
    }

    public List<TransactionHistoryResponseDto> getTransactionHistory(Long userId) {
        List<TransactionHistory> transactionHistories = transactionHistoryRepository.findByUserId(userId);

        return transactionHistories.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionHistoryResponseDto.class))
                .collect(Collectors.toList());
    }

}
