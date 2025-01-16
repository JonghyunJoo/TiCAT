package com.example.walletservice.service;

import com.example.walletservice.dto.TransactionHistoryResponseDto;
import com.example.walletservice.dto.WalletResponseDto;
import com.example.walletservice.entity.TransactionHistory;
import com.example.walletservice.entity.TransactionType;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.exception.CustomException;
import com.example.walletservice.exception.ErrorCode;
import com.example.walletservice.repository.TransactionHistoryRepository;
import com.example.walletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = Wallet.builder()
                .userId(1L)
                .balance(1000L)
                .build();
    }

    /**
     * 사용자가 이미 존재하면 WalletResponseDto로 반환되는지 테스트.
     * 이미 존재하는 사용자의 경우 WalletResponseDto가 반환되어야 한다.
     */
    @Test
    void shouldReturnWallet_whenWalletExists() {
        // Given: 존재하는 사용자 ID
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));
        when(modelMapper.map(any(Wallet.class), eq(WalletResponseDto.class))).thenReturn(WalletResponseDto.builder()
                .userId(1L)
                .balance(1000L)
                .build());

        // When: getBalance 메서드 호출
        WalletResponseDto walletResponseDto = walletService.createWallet(1L);

        // Then: 잔액 조회 결과 확인
        assertEquals(1000L, walletResponseDto.getBalance());
    }

    /**
     * 사용자가 존재하지 않으면 새 Wallet이 생성되는지 테스트.
     * 사용자가 없으면 새 Wallet이 생성되어 반환되어야 한다.
     */
    @Test
    void shouldCreateWallet_whenWalletNotFound() {
        // Given: 존재하지 않는 사용자 ID
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        // ModelMapper 설정을 명시적으로 지정
        WalletResponseDto mockResponse = WalletResponseDto.builder()
                .userId(1L)
                .balance(0L)
                .build();
        when(modelMapper.map(wallet, WalletResponseDto.class)).thenReturn(mockResponse);

        // When: createWallet 메서드 호출
        WalletResponseDto walletResponse = walletService.createWallet(1L);

        // Then: 새 Wallet이 생성되어 반환되는지 확인
        assertNotNull(walletResponse);
        assertEquals(1L, walletResponse.getUserId());
        assertEquals(0L, walletResponse.getBalance()); // 초기 잔액은 0이어야 함
        verify(walletRepository, times(1)).save(any(Wallet.class)); // save 메소드가 호출되었는지 확인
    }




    /**
     * 잔액 차감 시 정상적으로 처리되는지 테스트.
     * 잔액이 충분하면 차감하고, 차감 후 거래 내역이 저장되는지 확인.
     */
    @Test
    void shouldDeductBalance_whenSufficientBalance() {
        // Given: 차감할 금액
        Long amount = 500L;
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        // When: deductBalance 메서드 호출
        walletService.deductBalance(1L, amount);

        // Then: 잔액 차감 후 새 잔액 확인
        assertEquals(500L, wallet.getBalance());
        verify(transactionHistoryRepository, times(1)).save(any(TransactionHistory.class));
    }

    /**
     * 잔액이 부족할 경우 예외가 발생하는지 테스트.
     * 잔액이 부족하면 IllegalArgumentException이 발생해야 한다.
     */
    @Test
    void shouldThrowException_whenInsufficientBalance() {
        // Given: 차감할 금액이 잔액보다 많을 경우
        Long amount = 1500L;
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        // When / Then: 예외 발생
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> walletService.deductBalance(1L, amount));
        assertEquals("Insufficient balance", exception.getMessage());
    }

    /**
     * 잔액 충전 시 정상적으로 처리되는지 테스트.
     * 잔액 충전 후 거래 내역이 저장되는지 확인.
     */
    @Test
    void shouldChargeBalance_whenValidAmount() {
        // Given: 충전할 금액
        Long amount = 500L;
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet));

        // When: chargeWallet 메서드 호출
        walletService.chargeWallet(1L, amount);

        // Then: 잔액 충전 후 새 잔액 확인
        assertEquals(1500L, wallet.getBalance());
        verify(transactionHistoryRepository, times(1)).save(any(TransactionHistory.class));
    }

    /**
     * 거래 내역을 페이징 처리하여 조회할 수 있는지 테스트.
     * 사용자의 거래 내역을 페이징 처리하여 조회하는 기능이 정상 작동하는지 확인.
     */
    @Test
    void shouldGetTransactionHistory_whenValidUserId() {
        // Given: 거래 내역 페이징
        Pageable pageable = PageRequest.of(0, 10);
        Page<TransactionHistory> page = mock(Page.class);
        when(transactionHistoryRepository.findByUserId(1L, pageable)).thenReturn(page);
        when(page.stream()).thenReturn(Arrays.stream(new TransactionHistory[]{
                TransactionHistory.builder().amount(100L).build(),
                TransactionHistory.builder().amount(200L).build()
        }));

        // When: getTransactionHistory 메서드 호출
        List<TransactionHistoryResponseDto> result = walletService.getTransactionHistory(1L, 0, 10);

        // Then: 거래 내역이 반환되어야 함
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
