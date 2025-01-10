package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentResponseDto;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.exception.CustomException;
import com.example.paymentservice.exception.ErrorCode;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.client.ReservationClient;
import com.example.paymentservice.client.WalletClient;
import com.example.paymentservice.event.PaymentCancelledEvent;
import com.example.paymentservice.event.PaymentFailedEvent;
import com.example.paymentservice.event.PaymentSuccessEvent;
import com.example.paymentservice.messagequeue.PaymentEventProducer;
import com.example.paymentservice.vo.ReservationResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
    public PaymentResponseDto processPayment(Long reservationId, Long userId) {
        ReservationResponse reservation = reservationClient.getReservation(reservationId);

        Long walletBalance = walletClient.getBalance(userId);
        if (walletBalance < reservation.getAmount()) {
            PaymentFailedEvent failedEvent = new PaymentFailedEvent();
            failedEvent.setReservationId(reservation.getReservationId());
            failedEvent.setSeatId(reservation.getSeatId());
            failedEvent.setUserId(userId);
            paymentEventProducer.sendPaymentFailedEvent(failedEvent);

            throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        Payment payment = Payment.builder()
                .reservationId(reservation.getReservationId())
                .userId(userId)
                .amount(reservation.getAmount())
                .status(PaymentStatus.COMPLETED)
                .build();

        paymentRepository.save(payment);

        PaymentSuccessEvent successEvent = new PaymentSuccessEvent();
        successEvent.setReservationId(payment.getReservationId());
        successEvent.setSeatId(reservation.getSeatId());
        successEvent.setUserId(userId);
        successEvent.setAmount(reservation.getAmount());
        paymentEventProducer.sendPaymentSuccessEvent(successEvent);

        return modelMapper.map(payment, PaymentResponseDto.class);
    }

    @Override
    public void cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getStatus().equals(PaymentStatus.COMPLETED)) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        ReservationResponse reservation = reservationClient.getReservation(payment.getReservationId());

        PaymentCancelledEvent cancelledEvent = new PaymentCancelledEvent();
        cancelledEvent.setReservationId(payment.getReservationId());
        cancelledEvent.setSeatId(reservation.getSeatId());
        cancelledEvent.setUserId(payment.getUserId());
        paymentEventProducer.sendPaymentCancelledEvent(cancelledEvent);

        payment.updateStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
    }
}
