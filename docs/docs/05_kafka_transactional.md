# [MSA 관점에서의 트랜잭션 관리와 Saga 패턴 적용에 대한 설계 보고서]

## 1. 서론
- 본 보고서는 콘서트 티켓 예약 시스템의 마이크로서비스 아키텍처(MSA) 전환을 가정한다.
- MSA 관점에서 서비스 간 통신과 분산환 경에서의 트랜잭션 관리 방안 및 Saga 패턴의 적용에 대해 다룬다.
- 본 보고서는 '`PaymentService`' 의 결제 기능을 중점으로 설계에 대한 분석을 다루는 것을 목적으로 한다.
- 현재 시스템의 분석을 바탕으로 MSA 환경에서의 효과적인 트랜잭션 관리 전략에 대해 공부하고 정리한 것을 바탕으로 한다.

## 2. 현재 시스템 분석
### 2.1 아키텍처 개요
- 현재 '`PaymentService`' 시스템은 모놀리식 구조로, 다음과 같은 주요 컴포넌트로 구성되어 있다.

#### PaymentService
- 결제 처리의 핵심 로직을 담당하는 서비스
- 사용자 인증, 예약 확인, 결제 처리, 결제 내역 저장, 콘서트 상태 업데이트 등 전반적인 결제 프로세스를 조율한다. (Facade 의 역할)

#### UserService
- 사용자 정보 관리 담당
- 사용자 인증, 인가 등의 기능을 수행한다.

#### ReservationService
- 예약 정보 관리 담당
- 예약 생성, 조회, 상태 업데이트 등의 기능을 수행한다.

#### SeatService
- 콘서트의 좌석 관리 담당
- 좌석 조회, 좌석 상태 확인, 좌석 상태 업데이트 등의 기능을 수행한다. 

<br>

### 2.2 현재 트랜잭션 관리 현황 

- 현재 시스템의 핵심 트랜잭션은 `PaymentService` 의 `processPayment` 메서드에서 관리되고 있다.
- 이 메서드는 `@Transactional` 어노테이션을 통해 하나의 큰 트랜잭션으로 처리되고 있으며, 다음과 같은 주요 작업들을 포함한다

```Java
@Override
@Transactional
public PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto){
// 1. 사용자 및 예약 정보 조회
// 2. 결제 실행 및 결제 내역 저장
// 3. 예약 상태 업데이트
// 4. 좌석 상태 업데이트
    ...
}
```
- 이러한 구조는 데이터의 일관성을 유지하는 데 도움이 될 수 있지만, 여러 가지 문제점을 내포하고 있다. 아래에서 더 자세하게 알아보자.

<br>

### 2.3 현재 구조의 문제점

#### 1) 긴 트랜잭션으로 인한 성능 저하 가능성
- 하나의 트랜잭션 내에서 여러 복잡한 작업이 수행되므로, 트랜잭션의 지속 시간이 길어질 수 있다.
- 긴 트랜잭션은 데이터베이스 연결을 오랫동안 점유하게 되어, 전체 시스템의 처리량을 저하시킬 수 있다.
- 동시에 여러 결제 요청이 들어올 경우, 트랜잭션 간 경합이 발생하여 성능 저하가 심화될 수 있다.


#### 2) 여러 서비스 간의 강한 결합
- 하나의 트랜잭션 내에서 여러 서비스(User, Seat, Reservation, Payment)가 밀접하게 연관되어 있다.
- 이러한 강한 결합은 개별 서비스의 독립적인 변경이나 확장을 어렵게 만든다.
- 한 서비스의 변경이 다른 서비스에 영향을 미칠 가능성이 높아, 시스템 유지보수의 복잡성이 증가한다.


#### 3) Redis 작업 포함으로 인한 분산 트랜잭션 문제
- Redis를 사용한 좌석 잠금 처리가 동일한 트랜잭션 내에 포함되어 있어, 분산 트랜잭션 문제가 발생할 수 있다.
- 관계형 데이터베이스와 Redis 간의 트랜잭션 일관성을 보장하기 어려워, 데이터 불일치가 발생할 가능성이 있다.
- 네트워크 지연이나 Redis 서버 장애 시, 전체 트랜잭션이 실패할 위험이 있다.


#### 4) 단일 실패 지점(Single Point of Failure) 존재
- 모든 주요 로직이 하나의 서비스에 집중되어 있어, 이 서비스에 문제가 발생하면 전체 결제 시스템이 마비될 수 있다.
- 부분적인 기능 장애가 전체 시스템의 장애로 확대될 가능성이 높다.

#### 5) 개별 서비스의 독립적 확장 어려움
- 모든 기능이 하나의 서비스에 통합되어 있어, 특정 기능만을 선택적으로 확장하기 어렵다.
- 시스템의 일부분에 부하가 집중되더라도, 전체 시스템을 스케일아웃해야 하는 비효율성이 존재한다.
- 각 기능별로 다른 확장 전략을 적용하기 어려워, 리소스 활용의 최적화가 제한된다.

이러한 문제점들은 시스템의 확장성, 유연성, 그리고 장애 대응 능력을 제한하며, 향후 서비스의 성장과 변화에 대응하는 데 어려움을 줄 수 있다. 

따라서 이를 개선하기 위한 MSA 기반의 새로운 설계가 필요하다.

<br>

## 3. MSA로의 전환
- 현재의 모놀리식 구조에서 MSA 로의 전환은 시스템의 확장성, 유연성, 그리고 장애 대응 능력을 크게 향상시킬 수 있다. 
- 이 섹션에서는 MSA로의 전환 전략을 상세히 설명한다.

### 3.1 서비스 분리
현재 시스템을 다음과 같은 마이크로서비스로 분리한다.

#### 1) User Service
- 책임: 사용자 정보 관리, 인증 및 권한 처리
- 주요 기능:
  - 사용자 프로필 관리
  - 사용자 인증 및 권한 확인

#### 2) Concert Service
- 책임: 콘서트 정보 관리
- 주요 기능:
  - 콘서트 정보 관리
  - 콘서트 스케쥴 관리
  - 콘서트 상태 업데이트

#### 3) Seat Service
- 책임: 좌석 정보 관리
- 주요 기능:
  - 좌석 정보 관리 
  - 좌석 상태 관리

#### 3) Reservation Service
- 책임: 예약 관리
- 주요 기능:
  - 예약 생성 및 조회
  - 예약 상태 업데이트
  - 예약 취소 처리

#### 4) Payment Service
- 책임: 결제 처리, 결제 내역 관리
- 주요 기능:
  - 결제 실행 및 검증
  - 결제 내역 저장 및 조회
  - 결제 취소 처리

#### 5) Wallet Service
- 책임: 사용자의 잔액 관리
- 주요 기능:
    - 잔액 충전, 사용 처리
    - 잔액 조회
    - 충전 및 사용 내역 조회

#### 6) Queue Service
- 책임: 대기열 관리
- 주요 기능:
  - 대기열 토큰 생성 및 관리
  - 대기열 상태 업데이트
  - 대기열 우선순위 처리

<br>

### 3.2 트랜잭션 분리
각 서비스별로 트랜잭션을 분리하여 관리함으로써, 전체 시스템의 결합도를 낮추고 개별 서비스의 자율성을 높인다.

#### 1) Payment Transaction
- 범위: 결제 실행 및 결제 내역 저장
  ```Java
  @Transactional
  public PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto) {
  // 결제 로직 실행
  // 결제 내역 저장
  }
  ```

#### 2) Reservation Transaction
- 범위: 예약 상태 업데이트
  ```Java
    @Transactional
    public void completeReserve(Long reservationGroupId){

    }
  ```

#### 3) Concert Transaction
- 범위: 콘서트 상태 업데이트
  ```Java
  @Transactional
  public void reservationSuccess(List<Long> seatIdList) {
      // 콘서트 상태 업데이트 로직
  }
  ```

이러한 트랜잭션 분리를 통해 각 서비스는 자체적인 데이터 일관성을 유지하면서, 전체 시스템의 유연성과 확장성을 향상시킬 수 있다.

<br>

### 3.3 이벤트 기반 아키텍처 도입
- 서비스 간 통신을 위해 이벤트 기반 아키텍처를 도입하도록 설계한다.
- 이를 통해 서비스 간 결합도를 낮추고, 비동기적인 처리를 가능하게 한다.
- `Apache Kafka`를 활용해 시스템의 확장성, 신뢰성, 그리고 데이터 일관성을 향상시키는 데 중점을 두고 있다.

#### 3.3.1 이벤트 정의
- 결제 성공에 따른 예약 상태 전환 과정을 처리하기 위한 이벤트다.

```Java
@Data
public class PaymentSuccessEvent {
    private Long userId;
    private Long amount;
}
```
```Java
@Data
@Builder
public class ReservationSuccessEvent {
    private List<Long> seatIdList;
    private Long userId;
}
...
```

#### 3.3.2 이벤트 발행
- Payment SErvice의 `PaymentEventProducer`클래스에 정의한 `sendPaymentSuccessEvent()` 메서드를 통해 `payment_success_topic` 이벤트 발행
```Java
    public void sendPaymentSuccessEvent(PaymentSuccessEvent event) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            log.error("Error serializing PaymentSuccessEvent: ", ex);
        }

        kafkaTemplate.send("payment_success_topic", jsonInString);
        log.info("Sent PaymentSuccessEvent: {}", event);
    }
```

#### 3.3.3 이벤트 구독
- Reservation Service의 `PaymentKafkaListener`클래스에 정의한 `onPaymentSuccess()`메서드가 구독한 `payment_success_topic` 이벤트 처리
```Java
    @KafkaListener(topics = "payment_success_topic", groupId = "payment-service")
    public void onPaymentSuccess(String message) {
        try {
            PaymentSuccessEvent event = objectMapper.readValue(message, PaymentSuccessEvent.class);

            reservationService.completeReserve(event.getReservationGroupId());

            log.info("Payment success for reservation {} and user {}", event.getReservationGroupId(), event.getUserId());
        } catch (Exception e) {
            log.error("Error processing payment success message: {}", e.getMessage());
        }
    }
```

### 3.4 이벤트 처리의 이점
- 비동기 처리: 서비스 간 즉각적인 응답이 필요 없는 경우 비동기적으로 처리
- 느슨한 결합: 서비스 간 직접적인 의존성 제거
- 확장성: 새로운 기능 추가 시 기존 서비스 수정 없이 새로운 EventListener 추가 가능

이벤트 기반 아키텍처의 도입을 통해 시스템의 확장성과 유연성을 크게 향상시킬 수 있으며, 각 서비스의 자율성을 보장하면서도 전체 시스템의 일관성을 유지할 수 있을 것이라 기대한다.

<br>

## 4. MSA 에서 트랜잭션 관리하기 - Saga 패턴 적용

### 4.1 Saga 패턴 개요
- Saga 패턴은 마이크로서비스 아키텍처에서 분산 트랜잭션을 관리하기 위한 효과적인 방법이다. 이 패턴의 핵심 개념은 다음과 같다.

  - 로컬 트랜잭션 시퀀스: 하나의 큰 트랜잭션을 여러 개의 작은 로컬 트랜잭션으로 분할한다.
  - 보상 트랜잭션: 각 단계에서 실패가 발생할 경우, 이전 단계들의 변경사항을 취소하는 보상 트랜잭션을 실행한다.
  - 이벤트 기반 통신: 서비스 간 통신은 이벤트를 통해 이루어진다.


- Saga 패턴을 통해 다음과 같은 이점을 얻을 수 있을 것이라 기대한다.

  - 서비스 간 결합도 감소
  - 개별 서비스의 자율성 증가
  - 시스템 전체의 확장성 및 유연성 향상
  - 장애 상황에서의 복원력 증대

<br>

### 4.2 Saga 패턴 구현

#### 4.2.1 이벤트 정의
- 후술할 이벤트는 결제가 실패했을 때의 트랜잭션 롤백을 위한 이벤트이다.

```Java
@Data
public class PaymentFailedEvent {
    private Long reservationGroupId;
    private Long userId;
}
```
```Java
@Data
@Builder
public class ReservationCanceledEvent {
    private List<Long> seatIdList;
    private Long userId;
    private List<Long> reservationIdList;
}
```

- 각 이벤트의 목적과 의미:
  - PaymentFailedEvent: 결제 실패 시 발생하며, 실패한 예약 그룹 ID를 포함한다. 이를 통해 다른 서비스들이 적절한 보상 트랜잭션을 실행할 수 있다.
  - ReservationCanceledEvent: 결제 실패 혹은 예약 취소로 예약 상태가 'CANCELED' 상태로 업데이트되었음을 알리기 위해 발행한다.

이러한 이벤트 구조를 통해 각 서비스는 자신의 역할을 수행하고 실패 시 적절한 보상 조치를 취할 수 있다.

<br>

#### 4.2.2 서비스 구현
- 각 서비스는 특정 도메인의 로직을 처리하고, 관련 이벤트를 발행 및 구독하도록 한다.

**Payment Service**
```Java
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto paymentRequestDto) {
        Long userId = paymentRequestDto.getUserId();
        Long reservationGroupId = paymentRequestDto.getReservationGroupId();
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
```
- `processPayment` 메서드는 실제 결제 처리를 수행하고, 성공 또는 실패에 따라 적절한 이벤트를 발행하도록 한다.
- 트랜잭션 내에서 결제가 수행되며, 성공 시 `payment_success_topic`을, 실패 시 `payment_failed_topic`을 발행한다.


**Reservation Service**
```Java
    @KafkaListener(topics = "payment_failed_topic", groupId = "payment-service")
    public void onPaymentCanceled(String message) {
        try {
            PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);

            reservationService.cancelReservationGroup(event.getUserId(), event.getReservationGroupId());

            log.info("Payment failed for reservation {} and user {}", event.getReservationGroupId(), event.getUserId());
        } catch (Exception e) {
            log.error("Error processing payment failed message: {}", e.getMessage());
        }
    }
```
```Java
    @Transactional
    public void cancelReservationGroup(Long userId, Long reservationGroupId) {
        log.info("Canceling reservation group: {} for user: {}", reservationGroupId, userId);

        ReservationGroup reservationGroup = reservationGroupRepository.findById(reservationGroupId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_GROUP_NOT_FOUND));

        if (!reservationGroup.getUserId().equals(userId)) {
            log.warn("User {} is not authorized to cancel reservation group {}", userId, reservationGroupId);
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        reservationGroup.setStatus(ReservationStatus.CANCELLED);

        int updatedCount = reservationRepository.updateReservationStatusByGroupId(ReservationStatus.CANCELLED, reservationGroupId);
        log.info("Updated {} reservations to CANCELLED", updatedCount);

        List<Reservation> reservations = reservationRepository.findAllByReservationGroupId(reservationGroupId);
        List<Long> seatList = reservations.stream()
                .map(Reservation::getSeatId)
                .toList();

        List<Long> reservationIdList = reservations.stream()
                .map(Reservation::getId)
                .toList();

        ReservationCanceledEvent event = ReservationCanceledEvent.builder()
                .seatIdList(seatList)
                .userId(userId)
                .reservationIdList(reservationIdList)
                .build();

        reservationEventProducer.sendReservationCanceledEvent(event);
        log.info("Sent ReservationCanceledEvent for user: {}", userId);
    }
```
- `onPaymentCanceled()` 메서드는 `payment_failed_topic`토픽을 구독하고 `cancelReservationGroup()` 메서드를 실행시킨다
- `cancelReservationGroup()` 메서드는 실제 예약 상태 업데이트(예약 취소) 로직을 수행하고 `reservation_canceled_topic`을 발행한다

**Seat Service**
```Java
    @KafkaListener(topics = "reservation_canceled_topic", groupId = "reservation-service")
    public void onReservationCanceled(String message) {
        try {
            ReservationCanceledEvent event = objectMapper.readValue(message, ReservationCanceledEvent.class);

            for (Long seatId : event.getSeatIdList()) {
                seatService.cancelSeatLock(seatId);
                log.info("Seat lock canceled for seat ID: {}", seatId);
            }

            log.info("Reservation canceled for seats: {}", event.getSeatIdList());
        } catch (Exception e) {
            log.error("Error processing reservation canceled message: {}", e.getMessage(), e);
        }
    }
```
```Java
    @Transactional
    public void cancelSeatLock(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SEAT));

        seat.setSeatStatus(SeatStatus.AVAILABLE);
        seat.setUserId(null);
        log.info("Seat {} successfully unlocked", seatId);
    }
```
- `onReservationCanceled()` 메서드는 `reservation_canceled_topic`토픽을 구독하고 `cancelSeatLock()` 메서드를 실행시킨다.
- `cancelSeatLock()` 메서드는 실제 좌석 상태(AVAILABLE) 및 등록된 사용자 Id 업데이트 로직을 수행

<br>

### 4.2.3 보상 트랜잭션
- 각 단계에서 실패 시 이전 단계들의 작업을 취소하는 보상 트랜잭션은 해당 도메인 서비스에 위치한다.
- `Kafka`를 사용하여 발행한 실패 이벤트를 구독중인 서비스의 `KafkaListner`가 감지하고 처리한다.
- 이렇게 함으로써 각 서비스는 자신의 도메인에 대한 책임을 유지하면서도 전체 Saga 프로세스의 일관성을 보장할 수 있도록 한다.

#### 4.2.4 Saga 패턴으로 설계한 전체 프로세스의 흐름 정리  

1. 사용자가 결제를 시작하면 `PaymentService.processPayment()` 메서드가 호출된다.
2. `processPayment()`가 결제를 처리하고 결과 이벤트를 발행한다.
3. 결제 성공 시, `payment_success_topic`이 발행되고 이 토픽을 구독중인 Reservation Service와 Wallet Service에서 각각의 로직을 실행한다.
4. 결제 실패 시 `payment_failed_topic`이 발행되고 이 토픽을 구독중인 Reservation Service에서 보상 트랜잭션을 실행한다.
5. Reservation의 상태가 `CANCELED`로 업데이트 되고 `reservation_canceled_topic`이 발행된다.
6. `reservation_canceled_topic`을 구독 중인 Seat Service에서 보상 트랜잭션을 실시한다.
7. Seat의 상태가 초기화 되며 사실상 예약 및 결제 작업을 시작하기 전으로 되돌아 간다.

- 이 구조를 통해 전체 프로세스를 조율하면서도, 각 서비스의 자율성을 해치지 않을 것으로 기대된다.
- 또한, 각 단계에서 발생할 수 있는 실패에 대해 적절히 대응하여 시스템의 일관성을 유지할 것으로 기대된다. 
- 이벤트 기반 통신을 사용함으로써 서비스 간 결합도를 낮추고, 시스템의 확장성과 유연성을 높일 수 있을 것으로 기대된다.

<br>

## 5. 결론

- 본 보고서에서는 콘서트 티켓 예약 시스템의 MSA 전환 과정에서 서비스간 통신과 분산 환경에서의 트랜잭션 관리 방안 및 Saga 패턴의 적용에 대해 공부한 내용을 바탕으로 정리와 분석을 진행하고 설계하였다. 
- 주요 결론은 다음과 같다:

### MSA 전환의 필요성:
- 현재의 모놀리식 구조는 확장성, 유연성, 그리고 장애 격리에 한계가 있다. 
- MSA로의 전환을 통해 이러한 문제점들을 해결하고, 시스템의 전반적인 성능과 유지보수성을 향상시킬 수 있다고 기대한다.

### Saga 패턴의 효과성:
- 분산 환경에서의 트랜잭션 관리를 위해 Saga 패턴을 도입함으로써, 서비스 간 데이터 일관성을 유지하면서도 각 서비스의 자율성을 보장할 수 있다. 
- 이는 시스템의 확장성과 유연성을 크게 향상시킨다.

### 이벤트 기반 아키텍처의 이점:
- 이벤트 기반 아키텍처는 서비스 간 결합도를 낮추고, 시스템의 유연성을 높이는 데 효과적이라고 생각한다. 
- 특히 `Kafka`의 활용은 다음과 같은 이점을 얻을 수 있었다.
  - 높은 신뢰성: 메시지 손실 없이 안정적인 이벤트 처리
  - 확장성: Kafka를 통한 대규모 이벤트 처리 가능
  - 데이터 일관성: 트랜잭션 관리를 통한 데이터 일관성 보장
  - 시스템 분리: 결제 처리와 외부 시스템 사용의 분리

### 보상 트랜잭션의 구현:
- 각 서비스에 대한 보상 트랜잭션을 구현함으로써, 분산 환경에서의 데이터 일관성을 유지하고 시스템의 신뢰성을 보장할 수 있도록 한다.

### 마무리

- 결론적으로, 제안된 MSA 기반의 트랜잭션 관리 및 Saga 패턴 적용은 콘서트 티켓 예약 시스템의 확장성, 유연성, 그리고 장애 대응 능력을 크게 향상시킬 것으로 기대된다.

    