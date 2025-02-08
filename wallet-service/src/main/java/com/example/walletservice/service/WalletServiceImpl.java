package com.example.walletservice.service;

import com.example.walletservice.dto.TransactionHistoryResponseDto;
import com.example.walletservice.entity.TransactionHistory;
import com.example.walletservice.entity.TransactionType;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.exception.CustomException;
import com.example.walletservice.exception.ErrorCode;
import com.example.walletservice.repository.TransactionHistoryRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.dto.WalletResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private RedissonClient redissonClient;
    private ModelMapper modelMapper;

    @Transactional
    public WalletResponseDto createWallet(Long userId) {
        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
        if (walletOpt.isPresent()) {
            return modelMapper.map(walletOpt.get(), WalletResponseDto.class);
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(0L)
                .build();
        Wallet savedWallet = walletRepository.save(wallet);

        log.info("Wallet created for user {} with initial balance: {}", userId, savedWallet.getBalance());
        return modelMapper.map(savedWallet, WalletResponseDto.class);
    }

    // 잔액 충전
    @Transactional
    public Wallet chargeWallet(Long userId, Long amount) {
        String lockKey = "wallet-lock:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(1, 5, TimeUnit.SECONDS)) {
                throw new CustomException(ErrorCode.CONCURRENT_CHARGE_FAILED);
            }

            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

            wallet.setBalance(wallet.getBalance() + amount);
            walletRepository.save(wallet);

            saveTransactionHistory(userId, wallet.getBalance(), amount, TransactionType.CHARGE);

            log.info("Charged {} to user {} balance. New balance: {}", amount, userId, wallet.getBalance());
            return wallet;
        } catch (InterruptedException e) {
            log.error("Error while acquiring Redis lock for user {}: {}", userId, e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 잔액 차감
    @Transactional
    public Wallet deductBalance(Long userId, Long amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

        if (wallet.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        wallet.setBalance(wallet.getBalance() - amount);

        saveTransactionHistory(userId, wallet.getBalance(), amount, TransactionType.PAYMENT);

        log.info("Deducted {} from user {} balance. New balance: {}", amount, userId, wallet.getBalance());

        return wallet;
    }

    // 결제 취소 시 환불
    @Transactional
    public Wallet refundBalance(Long userId, Long amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

        wallet.setBalance(wallet.getBalance() + amount);

        saveTransactionHistory(userId, wallet.getBalance(), amount, TransactionType.REFUND);

        log.info("Refunded {} to user {} balance. New balance: {}", amount, userId, wallet.getBalance());

        return wallet;
    }

    // 거래 내역 저장
    private void saveTransactionHistory(Long userId, Long balance, Long amount, TransactionType transactionType) {
        TransactionHistory transactionHistory = TransactionHistory.builder()
                .amount(amount)
                .userId(userId)
                .createdAt(LocalDateTime.now(Clock.systemUTC()))
                .transactionType(transactionType)
                .balanceAfterTransaction(balance)
                .build();

        transactionHistoryRepository.save(transactionHistory);
    }

    // 잔액 조회
    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
    }

    public List<TransactionHistoryResponseDto> getTransactionHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionHistory> transactionHistories = transactionHistoryRepository.findByUserId(userId, pageable);
        return transactionHistories.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionHistoryResponseDto.class))
                .collect(Collectors.toList());
    }
}

