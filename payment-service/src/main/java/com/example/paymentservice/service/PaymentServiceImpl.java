package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentResponseDto;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.event.PaymentFailedEvent;
import com.example.paymentservice.exception.CustomException;
import com.example.paymentservice.exception.ErrorCode;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.client.ReservationClient;
import com.example.paymentservice.client.WalletClient;
import com.example.paymentservice.event.PaymentCanceledEvent;
import com.example.paymentservice.event.PaymentSuccessEvent;
import com.example.paymentservice.messagequeue.PaymentEventProducer;
import com.example.paymentservice.vo.ReservationResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationClient reservationClient;
    private final WalletClient walletClient;
    private final PaymentEventProducer paymentEventProducer;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(Long userId, Long reservationGroupId) {
        Long amount = reservationClient.getTotalPrice(reservationGroupId);

        Long walletBalance = walletClient.getBalance(userId);
        if (walletBalance < amount) {
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .reservationGroupId(reservationGroupId)
                    .userId(userId)
                    .build();
            paymentEventProducer.sendPaymentFailedEvent(failedEvent);
            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        Payment payment = Payment.builder()
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.COMPLETED)
                .build();

        paymentRepository.save(payment);

        paymentEventProducer.sendPaymentSuccessEvent(
                PaymentSuccessEvent.builder()
                .userId(userId)
                .amount(amount)
                .build());

        return modelMapper.map(payment, PaymentResponseDto.class);
    }

    @Override
    @Transactional
    public void cancelPayment(List<Long> reservationIds, Long userId) {
        log.info("Canceling payments for reservations: {} by user: {}", reservationIds, userId);

        List<ReservationResponse> reservations = reservationIds.stream()
                .map(reservationClient::getReservation)
                .toList();

        long totalAmount = reservations.stream()
                .mapToLong(ReservationResponse::getPrice)
                .sum();

        PaymentCanceledEvent canceledEvent = PaymentCanceledEvent.builder()
                .userId(userId)
                .amount(totalAmount)
                .build();

        paymentEventProducer.sendPaymentCanceledEvent(canceledEvent);
        log.info("Sent PaymentCanceledEvent for user: {}, amount: {}", userId, totalAmount);

        Payment payment = Payment.builder()
                .userId(userId)
                .amount(totalAmount)
                .status(PaymentStatus.CANCELLED)
                .build();

        paymentRepository.save(payment);
        log.info("Saved canceled payment for user: {}, amount: {}", userId, totalAmount);
    }
}
