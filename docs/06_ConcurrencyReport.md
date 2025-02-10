# 동시성 문제와 극복에 관한 문서

## 1. 콘서트 대기열 시스템에서 동시성 문제가 발생할 수 있는 로직

### 1) 좌석예약기능
- 동시에 여러명이 하나의 좌석을 두고 예약하려고 하면 단 1명만이 그 좌석을 예약할 수 있도록 해야한다.

#### 동시성 문제 발생 이유:
- 여러 사용자가 동시에 같은 좌석을 예약하려고 시도할 때, 시스템이 각 요청을 순차적으로 처리하지 않으면 중복 예약이 발생할 수 있다.
- 데이터베이스 트랜잭션이 적절히 관리되지 않으면, 한 사용자의 예약 과정 중 다른 사용자가 같은 좌석을 예약할 수 있다.

#### 기대하는 결과:
- 특정 좌석에 대해 최초로 예약 요청을 완료한 사용자만 해당 좌석을 성공적으로 예약을 한다.
- 다른 사용자들의 동일 좌석 예약 시도는 실패하고, 적절한 오류 메시지를 받아야 한다.

### 2) 잔액 충전
- 한 명의 유저가 자신의 잔액을 충전을 할 때, 실수로 여러번을 호출할 경우에 1회만 가능하도록 해야한다.

#### 동시성 문제 발생 이유:
- 사용자가 실수로 또는 네트워크 지연으로 인해 충전 버튼을 여러 번 클릭할 경우, 각 요청이 독립적으로 처리되어 중복 충전이 발생할 수 있다.
- 서버에서 요청을 처리하는 동안 클라이언트 측에서 추가 요청을 보내면, 서버가 이전 요청의 처리 상태를 확인하지 않고 새 요청을 처리할 수 있다.

#### 기대하는 결과:
- 사용자가 여러 번 충전 요청을 보내더라도 단 한 번만 잔액이 증가해야 한다.
- 충전 금액은 정확히 한 번만 사용자의 계정에 반영되어야 하며, 금액 오차가 없어야 한다.

<br>
        
## 2. 동시성 이슈 대응 이전의 로직

- 원래 로직은 Service 계층에 @Transactional 어노테이션을 적용하여 트랜잭션을 관리했다.
- 이러한 접근 방식을 선택한 이유는 아래와 같다.

### 트랜잭션 관리 방식 선택 이유

1. 원자성 보장: 하나의 트랜잭션 내에서 Service 레이어의 모든 로직이 원자성을 가지고 실행되어야 한다고 판단했다.
2. 단순성: 서비스 계층에 트랜잭션을 적용함으로써 모든 데이터베이스 연산이 하나의 트랜잭션으로 묶이도록 했다.
3. 일관성: 모든 비즈니스 로직이 하나의 트랜잭션 내에서 실행되므로, 데이터의 일관성을 유지하기 쉽다고 생각했다.

### 이 접근 방식의 문제점

1. 트랜잭션 범위가 너무 넓음:
    - 서비스 계층의 메서드 전체가 하나의 트랜잭션으로 묶여 있어, 불필요하게 긴 시간 동안 데이터베이스 리소스를 점유할 수 있다.

2. 동시성 제어의 어려움:
    - 넓은 트랜잭션 범위로 인해 동시에 여러 요청이 처리될 때 데드락이 발생하거나 성능이 저하될 수 있다.

3. 세밀한 제어의 부재:
    - 특정 연산에 대해서만 트랜잭션을 적용하거나, 다른 격리 수준을 설정하는 등의 세밀한 제어가 어렵다.

4. 성능 저하:
    - 모든 연산이 하나의 큰 트랜잭션으로 묶여 있어, 데이터베이스 연결이 오래 유지되면서 전반적인 시스템 성능이 저하될 수 있다.

이러한 문제점들로 인해 동시성 이슈가 발생할 가능성이 높아지며, 특히 높은 트래픽 상황에서 시스템의 안정성과 성능이 저하될 수 있다.
<br>
따라서 동시성 문제에 대응하기 위해서는 트랜잭션의 범위를 좁히고, 더 세밀한 동시성 제어 메커니즘을 도입할 필요가 있다.

<br>

## 3. DB 락 구현

### 낙관적 락 (Optimistic Lock)

- 낙관적 락은 동시 업데이트가 드물게 발생한다는 가정 하에 동작한다.
- 이 방식은 데이터 수정 시 충돌이 발생하지 않을 것이라고 이름 그대로 '낙관적으로' 가정하고, 충돌이 발생했을 때 이를 감지하고 처리한다.

#### 코드 구현

```Java
@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Setter
    private Long balance;

    @Version
    private Integer version; //낙관적 락 적용
}
```
```Java
@Service 
public class WalletServiceImpl implements WalletService() { 
    private final WalletRepository walletRepository; 
    private final TransactionHistoryRepository transactionHistoryRepository;

    @Transactional
    public void chargeWallet(Long userId, Long amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));

        Long oldBalance = wallet.getBalance();

        wallet.setBalance(oldBalance + amount);

        //낙관적 락 적용
        try {
            walletRepository.save(wallet);
        } catch (OptimisticLockException e) {
            throw new CustomException(ErrorCode.CONCURRENT_CHARGE_FAILED);
        }

        saveTransactionHistory(userId, wallet, amount, TransactionType.CHARGE);

        log.info("Charged {} to user {} balance. New balance: {}", amount, userId, wallet.getBalance());
    }
}
```
1. `Wallet` 클래스:
    - `@Version`을 활용해 동시성 충돌 감지.
    - 이 버전 필드는 JPA에 의해 자동으로 관리되며, 엔티티가 업데이트될 때마다 증가한다.

2. `WalletService` 클래스:
    - `chargeWallet()` 메서드에 `@Transactional` 어노테이션을 적용하여 트랜잭션 범위를 좁혔다.
    - `walletRepository.save(wallet)` 메서드를 실행할 때 OptimisticLockException이 발생하면 예외를 던지도록 했다.
    
#### 주요 변경 사항

1. 트랜잭션 범위 축소:
    - 트랜잭션의 범위를 Service에서 Manager로 내려 더 작은 단위로 제어하도록 했다.
    - 이를 통해 트랜잭션 유지 시간을 줄이고, 리소스 점유를 최소화했다.

2. 낙관적 락 구현:
    - 엔티티에 버전 정보를 추가하여 JPA의 낙관적 락 기능을 활용했다.
    - 동시 수정 시 발생하는 충돌을 감지하고 예외를 발생시킨다.

3. 예외 처리:
    - 낙관적 락 실패 시 발생하는 예외를 커스텀한 예외를 뱉도록 명시적으로 처리하여 사용자에게 적절한 응답을 제공하도록 했다.

#### 테스트 코드 - 성공케이스 - 10번의 시도

```Java
    @BeforeEach
    @Transactional
    public void setUp() {
        // Given: 초기 지갑 데이터 설정
        walletRepository.deleteAll();
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(initialBalance)
                .version(0) // 초기 버전 설정
                .build();
        walletRepository.save(wallet);
    }

    @Test
    public void 동시에_10번_충전_시도하면_1번만_성공해야_한다() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When: 10개의 충전 요청을 동시에 실행
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    walletService.chargeWallet(userId, chargeAmount);
                } catch (CustomException | OptimisticLockingFailureException e) {
                    log.info("충전 실패: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        // Then: 충전이 1번만 성공했는지 검증
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        assertThat(wallet.getBalance()).isEqualTo(initialBalance + chargeAmount);

        log.info("최종 잔액: {}", wallet.getBalance());
    }

```
#### 테스트 코드 설명
- 10개의 스레드를 사용하여 동시에 잔액 충전을 시도한다.
- 각 스레드는 5000원씩 충전을 시도한다.
- 테스트 결과, 최종 잔액이 15000원(초기 10000원 + 1회 성공한 5000원)임을 확인한다.




#### 테스트 코드 - 실패케이스 - 100번의 시도

```Java
    @Test
    public void 동시에_10번_충전_시도하면_1번만_성공해야_한다() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When: 10개의 충전 요청을 동시에 실행
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    walletService.chargeWallet(userId, chargeAmount);
                } catch (CustomException | OptimisticLockingFailureException e) {
                    log.info("충전 실패: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown();

        // Then: 충전이 1번만 성공했는지 검증
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();
        assertThat(wallet.getBalance()).isEqualTo(initialBalance + chargeAmount);

        log.info("최종 잔액: {}", wallet.getBalance());
    }
```

- 100회가 되었을 때 테스트가 깨지게 된다.

#### 결론 및 고찰

1. 낙관적 락의 효과와 한계:
    - 10회 정도의 동시 요청에 대해서는 낙관적 락이 효과적으로 동작함을 확인했다.
    - 하지만 100회의 동시 요청 테스트에서는 실패가 발생했다. 이는 낙관적 락의 한계를 보여준다.


2. 동시성 증가에 따른 문제:
    - 동시 요청 수가 증가함에 따라 충돌 발생 확률이 높아진다.
    - 충돌이 발생할 때마다 예외가 발생하고 재시도가 필요하므로, 로직이 복잡한경우 전체적인 처리 시간이 길어질 수 있다.
    - 극단적인 경우, 모든 요청이 계속 충돌하여 결과적으로 처리되지 못하는 상황(livelock)이 발생할 수 있다.


3. 성능과 정확성의 트레이드오프:
    - 낙관적 락은 충돌이 적은 환경에서는 높은 성능을 제공한다.
    - 그러나 충돌이 빈번한 환경에서는 재시도로 인한 오버헤드가 크게 증가할 수 있다.


4. 실제 운영 환경 고려사항:
    - 실제 서비스의 동시 요청 패턴과 빈도를 분석하여 적절한 동시성 제어 메커니즘을 선택해야 한다.


결론적으로, 낙관적 락은 간단하고 효과적인 동시성 제어 방법이지만, 높은 동시성 환경에서는 한계가 있음을 확인했다.

---

### 비관적 락 (Pessimistic Lock)

- 비관적 락은 동시 업데이트가 빈번하게 발생할 것이라고 '비관적으로' 가정하고, 데이터를 읽는 시점에 락을 걸어 다른 트랜잭션의 접근을 차단한다.
- 이 방식은 데이터 무결성을 강하게 보장하지만, 동시성 처리 성능이 낮아질 수 있다.
<br>

#### 코드 구현

```Java
@Service  
public class SeatServiceImpl implements SeatService {
    private final SeatRepository seatRepository;

    @Transactional
    public void lockSeat(Long userId, Long seatId) {
        Seat seat = seatRepository.findByIdWithLock(seatId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SEAT));

        if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_LOCKED);
        }

        seat.setSeatStatus(SeatStatus.LOCKED);
        seat.setUserId(userId);
        seatRepository.save(seat);

        log.info("Seat {} successfully locked by user {}", seatId, userId);
    }
}

```Java
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :seatId")
    Optional<Seat> findByIdWithLock(@Param("seatId") Long seatId);
}
```    

1. `SeatService` 클래스:
    - `lockSeat` 메서드에 `@Transactional` 어노테이션을 적용하여 트랜잭션 범위를 좁혔다.
    - `findByIdWithLock` 메서드를 사용하여 비관적 락이 걸린 상태로 Seat 엔티티를 조회하도록 했다.
    - 좌석 상태를 확인하고 업데이트하는 로직을 추가했다.

3. Repository 클래스:
    - `@Lock(LockModeType.PESSIMISTIC_WRITE)` 어노테이션을 사용하여 비관적 락을 구현했다.
    - 처음에는 `PESSIMISTIC_WRITE` 로 했다가 `PESSIMISTIC_READ`로 변경하여 성능을 개선했다.
    - 그 이유는, `PESSIMISTIC_WRITE` 를 사용할 만큼 데이터 무결성이 필요하다고 생각하지 않아서였다.

<br>

#### 테스트

- 1000개의 스레드를 사용하여 동시에 좌석 예약을 시도한다.
- 테스트 결과, 단 하나의 예약만 성공하고 나머지는 실패함을 확인한다.
- 성공한 예약에 대해서는 좌석 상태가 'UNAVAILABLE'로 변경됨을 검증한다.
- 실행 시간을 측정하여 성능을 평가하도록 했다.

<br>

#### 결과 및 고찰

1. 정확성:
    - 비관적 락은 1000개의 동시 요청 중 정확히 1개만 성공하도록 보장했다.
    - 이는 비관적 락이 높은 동시성 환경에서도 데이터 무결성을 확실히 보장함을 보여주는 것이다.

2. 성능:
    - `PESSIMISTIC_WRITE`를 사용했을 때 752 밀리초가 소요되었다.
    - `PESSIMISTIC_READ`로 변경 후 695 밀리초로 약간의 성능 향상이 있었지만, 그 차이는 미미했다.

3. 트레이드오프:
    - 비관적 락은 데이터 정합성을 강력하게 보장하지만, 동시에 처리할 수 있는 트랜잭션의 수가 제한된다.
    - 높은 동시성 환경에서는 전체적인 시스템 처리량이 낮아질 수 있다.

4. 사용 시나리오:
    - 데이터 정합성이 매우 중요하고, 충돌이 자주 발생하는 환경에서 유용하다.
    - 예를 들어, 콘서트 티켓 예매와 같이 제한된 리소스에 대한 경쟁이 심한 경우에 적합한 것으로 보인다.

5. 확장성 고려:
    - 비관적 락은 데이터베이스 수준의 락을 사용하므로, 분산 환경에서의 확장성에 제한이 있을 수 있다.(MSA 구조인 현재 프로젝트에서 적합하지 않음)
      => 분산 및 대규모 시스템에서는 분산 락과의 조합을 고려해볼 수 있다.
<br>

## 4. 분산 락 구현

- 분산 락은 여러 서버나 인스턴스에서 동시에 접근하는 리소스에 대한 동시성을 제어하기 위해 사용된다.
- Redisson를 사용하여 분산 환경에서도 안전하게 동작

```Java
    @Transactional
    public void chargeWallet(Long userId, Long amount) {
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

            saveTransactionHistory(userId, wallet, amount, TransactionType.CHARGE);

            log.info("Charged {} to user {} balance. New balance: {}", amount, userId, wallet.getBalance());

        } catch (InterruptedException e) {
            log.error("Error while acquiring Redis lock for user {}: {}", userId, e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
```
<br>

### 구현 방식
    - Redisson의 tryLock()을 사용하여 동시에 하나의 요청만 처리
    - 락 획득 실패 시 예외 발생 → 중복 충전 방지
    - 트랜잭션 내에서 락 해제 보장 (finally 블록 활용)

<br>

### 테스트 결과
    - 1000개의 동시 요청 중 1개만 성공하고 나머지는 실패하는 것을 확인했다.
    - 최종 잔액이 정확히 한 번의 충전만 반영되었음을 검증했다.

<br>

### 결론 및 고찰

1. 분산 환경 대응:
    - Redis를 이용한 분산 락은 여러 서버에서 동작하는 애플리케이션의 동시성 문제를 효과적으로 해결할 수 있다.

2. 성능과 신뢰성:
    - 분산 락을 통해 데이터의 정합성을 보장하면서도, Redis의 빠른 처리 속도로 인해 성능 저하를 최소화할 수 있었다.
    - 낙관적 락보다 성능 개선 → 재시도 필요 없이 충돌을 미리 차단
    - 그러나 Redis 서버의 장애 상황에 대한 대비책도 고려해야 한다.


<br>

## 5. 내가 선택한 콘서트 예약의 동시성 처리 방법

### 1) 예약 기능 구현

1. 동시성 제어 전략
    - Redis를 이용한 분산 락 (DistributedSimpleLock): 대규모 동시 요청 처리
    - 비관적 락 (PESSIMISTIC_READ): 데이터베이스 수준에서의 동시성 제어
    - 이중 락 전략: Redis 장애 시 비관적 락으로 대체 가능하도록 함

<br>

2. 주요 구현 로직
    - 분산 락을 이용한 좌석 잠금 요청 격리
    - 트랜잭션 내에서 좌석 상태 확인 및 업데이트
    - 좌석을 잠금 상태로 변환

```Java
@Transactional
public void lockSeat(Long userId, Long seatId) {
    String lockKey = "seat-lock:" + seatId;
    RLock lock = redissonClient.getLock(lockKey);

    boolean isRedisLockAcquired = false;

    try {
        isRedisLockAcquired = lock.tryLock(1, 5, TimeUnit.SECONDS);

        if (isRedisLockAcquired) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SEAT));

            if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
                throw new CustomException(ErrorCode.SEAT_ALREADY_LOCKED);
            }

            seat.setSeatStatus(SeatStatus.LOCKED);
            seat.setUserId(userId);
            seatRepository.save(seat);

            log.info("Seat {} successfully locked by user {} using Redisson", seatId, userId);
        } else {
            log.warn("Redisson lock failed for seat {}, using DB pessimistic lock instead", seatId);

            Seat seat = seatRepository.findByIdWithLock(seatId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_SEAT));

            if (seat.getSeatStatus() != SeatStatus.AVAILABLE) {
                throw new CustomException(ErrorCode.SEAT_ALREADY_LOCKED);
            }

            seat.setSeatStatus(SeatStatus.LOCKED);
            seat.setUserId(userId);
            seatRepository.save(seat);

            log.info("Seat {} successfully locked by user {} using DB pessimistic lock", seatId, userId);
        }
    } catch (InterruptedException e) {
        log.error("Error while acquiring Redis lock for seat {}: {}", seatId, e.getMessage());
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    } finally {
        if (isRedisLockAcquired && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}

```

<br>

### 2) 잔액 충전 기능 구현
1. 동시성 제어 전략
    - Redis를 이용한 분산 락: 동시 충전 요청 제어
    - 트랜잭션 격리: 데이터베이스 수준에서의 동시성 제어

<br>

2. 주요 로직
    - 충전 요청 검증
    - 분산 락을 이용한 충전 요청 격리
    - 트랜잭션 내에서 잔액 업데이트

```Java
    @Transactional
    public void chargeWallet(Long userId, Long amount) {
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

            saveTransactionHistory(userId, wallet, amount, TransactionType.CHARGE);

            log.info("Charged {} to user {} balance. New balance: {}", amount, userId, wallet.getBalance());

        } catch (InterruptedException e) {
            log.error("Error while acquiring Redis lock for user {}: {}", userId, e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
```

### 구현 결정 사항 및 이유

1. 예약 기능의 동시성 제어 :
    - 이중 락 전략 (분산 락 + 비관적 락):
    - Redis 분산 락으로 1차 동시성 제어를 수행한다.
    - 비관적 락으로 2차 안전장치를 마련하여 데이터 정합성을 보장한다.

2. 잔액 충전의 동시성 제어:
    - 분산 락만을 사용하여 동시 충전 요청을 제어한다.
    - 여러 번의 충전 요청 중 한 번만 성공하도록 하여 우발적인 중복 충전을 방지한다.

## 최종 결론

1. 동시성 제어의 효과적인 구현:
    - 분산 락과 비관적 락의 조합을 통해 동시성 제어의 목적을 달성했다.
    - 대규모 동시 요청 상황에서도 데이터 정합성을 유지할 수 있음을 확인했다.

2. 성능과 정확성의 균형:
    - Redis를 활용한 분산 락으로 빠른 응답 시간을 유지하면서도 정확한 동시성 제어를 구현했다.
    - 비관적 락을 2차 안전장치로 사용하여 데이터베이스 수준의 안전성을 보장하도록 했다.

