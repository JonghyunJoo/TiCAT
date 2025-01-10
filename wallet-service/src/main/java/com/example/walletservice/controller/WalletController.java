package com.example.walletservice.controller;

import com.example.walletservice.dto.TransactionHistoryResponseDto;
import com.example.walletservice.service.WalletService;
import com.example.walletservice.vo.ResponseWallet;
import com.example.walletservice.vo.WalletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@AllArgsConstructor
@Tag(name = "Wallet API", description = "사용자 지갑 관리 API")
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "지갑 생성", description = "사용자의 새로운 지갑을 생성합니다.")
    @PostMapping
    public ResponseEntity<ResponseWallet> createWallet(@RequestHeader(value = "X-User-Id") Long userId) {
        ResponseWallet responseWallet = walletService.createWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseWallet);
    }

    @Operation(summary = "잔액 조회", description = "사용자의 현재 잔액을 조회합니다.")
    @GetMapping("/balance/{userId}")
    public ResponseEntity<Long> getBalance(@PathVariable Long userId) {
        Long balance = walletService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @Operation(summary = "잔액 충전", description = "사용자의 지갑 잔액을 충전합니다.")
    @PutMapping("/charge")
    public ResponseEntity<String> chargeWallet(@RequestHeader(value = "X-User-Id") Long userId, @RequestBody WalletRequest walletRequest) {
        walletService.chargeWallet(userId, walletRequest.getAmount());
        return ResponseEntity.ok("Wallet charged successfully.");
    }

    @Operation(summary = "거래 내역 조회", description = "사용자의 거래 내역을 조회합니다.")
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionHistoryResponseDto>> getTransactionHistory(@RequestHeader(value = "X-User-Id") Long userId) {
        List<TransactionHistoryResponseDto> history = walletService.getTransactionHistory(userId);
        return ResponseEntity.ok(history);
    }
}
