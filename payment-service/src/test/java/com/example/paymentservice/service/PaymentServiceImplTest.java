package com.example.paymentservice.service;

import com.example.paymentservice.client.ReservationClient;
import com.example.paymentservice.client.WalletClient;
import com.example.paymentservice.dto.PaymentResponseDto;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.event.PaymentCancelledEvent;
import com.example.paymentservice.event.PaymentFailedEvent;
import com.example.paymentservice.event.PaymentSuccessEvent;
import com.example.paymentservice.exception.CustomException;
import com.example.paymentservice.exception.ErrorCode;
import com.example.paymentservice.messagequeue.PaymentEventProducer;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.vo.ReservationResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationClient reservationClient;

    @Mock
    private WalletClient walletClient;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    /**
     결제가 성공적으로 처리되는 경우
     **/
    @Test
    void shouldProcessPaymentSuccessfullyWhenBalanceIsSufficient() {
        // Given: 결제 정보를 미리 설정
        Long reservationId = 1L;
        Long userId = 2L;
        Long amount = 100L;

        ReservationResponse reservation = new ReservationResponse();
        reservation.setReservationId(reservationId);
        reservation.setSeatId(10L);
        reservation.setAmount(amount);

        Mockito.when(reservationClient.getReservation(reservationId)).thenReturn(reservation);
        Mockito.when(walletClient.getBalance(userId)).thenReturn(200L);

        Payment payment = Payment.builder()
                .reservationId(reservationId)
                .userId(userId)
                .amount(amount)
                .status(PaymentStatus.COMPLETED)
                .build();
        Mockito.when(paymentRepository.save(Mockito.any(Payment.class))).thenReturn(payment);

        PaymentResponseDto expectedResponse = new PaymentResponseDto();
        expectedResponse.setAmount(amount);
        Mockito.when(modelMapper.map(Mockito.any(), Mockito.any())).thenReturn(expectedResponse);

        // When: 결제를 처리
        PaymentResponseDto response = paymentService.processPayment(reservationId, userId);

        // Then: 결과 확인
        Assertions.assertNotNull(response);  // 응답이 null이 아닌지 확인
        Assertions.assertEquals(amount, response.getAmount());
        Mockito.verify(paymentRepository, Mockito.times(1)).save(Mockito.any(Payment.class));
        Mockito.verify(paymentEventProducer, Mockito.times(1)).sendPaymentSuccessEvent(Mockito.any(PaymentSuccessEvent.class));
    }

    /**
     잔액이 부족하여 결제가 실패하는 경우
     **/
    @Test
    void shouldFailProcessPaymentWhenBalanceIsInsufficient() {
        // Given: 잔액이 부족한 상태 설정
        Long reservationId = 1L;
        Long userId = 2L;

        ReservationResponse reservation = new ReservationResponse();
        reservation.setReservationId(reservationId);
        reservation.setSeatId(10L);
        reservation.setAmount(200L);

        Mockito.when(reservationClient.getReservation(reservationId)).thenReturn(reservation);
        Mockito.when(walletClient.getBalance(userId)).thenReturn(100L);

        // When / Then: 결제 처리 시도 및 예외 발생 확인
        CustomException exception = Assertions.assertThrows(CustomException.class, () ->
                paymentService.processPayment(reservationId, userId)
        );

        Assertions.assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        Mockito.verify(paymentEventProducer, Mockito.times(1)).sendPaymentFailedEvent(Mockito.any(PaymentFailedEvent.class));
    }

    /**
     결제가 성공적으로 취소되는 경우
     **/
    @Test
    void shouldCancelPaymentSuccessfullyWhenPaymentExists() {
        // Given: 취소 대상 결제 정보 설정
        Long paymentId = 1L;

        Payment payment = Payment.builder()
                .id(paymentId)
                .reservationId(10L)
                .userId(2L)
                .amount(100L)
                .status(PaymentStatus.COMPLETED)
                .build();

        ReservationResponse reservation = new ReservationResponse();
        reservation.setReservationId(10L);
        reservation.setSeatId(20L);

        Mockito.when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        Mockito.when(reservationClient.getReservation(10L)).thenReturn(reservation);

        // When: 결제 취소 처리
        paymentService.cancelPayment(paymentId);

        // Then: 결과 확인
        Assertions.assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        Mockito.verify(paymentRepository, Mockito.times(1)).save(Mockito.any(Payment.class));
        Mockito.verify(paymentEventProducer, Mockito.times(1)).sendPaymentCancelledEvent(Mockito.any(PaymentCancelledEvent.class));
    }
}
