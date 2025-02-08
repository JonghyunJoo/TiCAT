package com.example.walletservice.messagequeue;

import com.example.walletservice.entity.Wallet;
import com.example.walletservice.event.PaymentCanceledEvent;
import com.example.walletservice.event.PaymentSuccessEvent;
import com.example.walletservice.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaListener {

    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    // 결제 성공 시 처리
    @KafkaListener(topics = "payment_success_topic", groupId = "payment-service")
    public void onPaymentSuccess(String message) {
        try {
            PaymentSuccessEvent paymentSuccessEvent = objectMapper.readValue(message, PaymentSuccessEvent.class);

            Wallet wallet = walletService.deductBalance(
                    paymentSuccessEvent.getUserId(),
                    paymentSuccessEvent.getAmount());

            log.info("Payment success for user {}, {}, After : {}",
                    wallet.getUserId(), paymentSuccessEvent.getAmount(), wallet.getBalance());
        } catch (Exception e) {
            log.error("Error processing payment success message: {}", e.getMessage());
        }
    }

    // 결제 취소 시 처리
    @KafkaListener(topics = "payment_canceled_topic", groupId = "payment-service")
    public void onPaymentCanceled(String message) {
        try {
            PaymentCanceledEvent paymentCanceledEvent = objectMapper.readValue(message, PaymentCanceledEvent.class);

            Wallet wallet = walletService.refundBalance(
                    paymentCanceledEvent.getUserId(),
                    paymentCanceledEvent.getAmount());

            log.info("Payment canceled for user {}, {}, After : {}",
                    wallet.getUserId(), paymentCanceledEvent.getAmount(), wallet.getBalance());
        } catch (Exception e) {
            log.error("Error processing payment canceled message: {}", e.getMessage());
        }
    }
}
