# 많은 수의 인원을 수용할 수 있는 대기열 시스템을 위한 Redis 이관에 관한 보고서

## 1> 개요
- 본 보고서는 기존 DB 기반의 콘서트 대기열 시스템을 Redis를 활용한 시스템으로 변경하는 과정을 상세히 기술한다. 
- 이 변경은 대규모 인원을 수용할 수 있는 효율적인 대기열 시스템 구축을 목표로 진행되었다.

<br>

## 2> 기존 시스템 분석
### 2.1 기존 시스템의 구조

- JPA를 사용한 RDB 기반 구현
- `Queue` 엔티티를 통한 대기열 정보 저장
- `QueueJpaRepository`를 통한 데이터 접근 및 조작

### 2.2 한계점

- 확장성 제한:
  - 관계형 DB는 대규모 동시 접속 처리에 한계가 있음
  - Scale-Out 시 데이터베이스에 대기열를 저장할 때 순서 보장이나 데이터의 정합성이 훼손될 수 있음
  - 근거: 
    - 관계형 DB는 ACID 속성을 보장하기 위해 락(lock)을 사용하며, 이는 동시성 처리에 병목현상을 일으킬 수 있다.
    - 예를 들어, 대기열 순서 업데이트 시 row-level 락으로 인해 처리 속도가 저하될 수 있다.


- 성능 이슈: 
  - 대기열 위치 조회 등에 복잡한 쿼리가 필요해 성능 저하 가능성
  - 근거: 대기열 위치 조회 시 `SELECT COUNT(*) FROM Queue WHERE id < ? AND status = 'WAITING'`와 같은 쿼리가 필요하며, 이는 대기열 크기가 커질수록 성능이 저하된다.


- 실시간 처리의 어려움: 
  - 대기열 상태 변경 및 조회에 지연 발생 가능
  - 근거: 
    - 트랜잭션 처리 및 커밋 과정에서 발생하는 지연으로 인해 실시간성이 떨어질 수 있다. 
    - 예를 들어, 대기열 상태 업데이트 후 즉시 조회 시 최신 상태가 반영되지 않을 수 있다.

<br>

## 3> Redis 기반 시스템으로의 전환
### 3.1 Redis 선택 이유

- 고성능: 
  - 메모리 기반 데이터 구조로 빠른 읽기/쓰기 가능
  - Redis는 초당 100,000개 이상의 읽기/쓰기 작업을 처리할 수 있으며, 대규모 동시 접속 상황에서도 빠른 응답 시간을 유지할 수 있다.


- 확장성: 
  - 대규모 동시 접속 처리에 적합
  - AWS 의 ElastiCache 등 Redis Cluster를 사용하여 수평적 확장이 가능하며, 필요에 따라 노드를 추가하여 처리 용량을 늘릴 수 있다.


- 실시간 처리: 
  - 대기열 상태 변경 및 조회를 즉시 반영 가능 
  - Redis의 `ZADD`, `ZRANK` 명령어를 사용하여 대기열 추가 및 위치 조회를 밀리초 단위의 지연으로 처리할 수 있다.


- 데이터 구조의 다양성: 
  - Sorted Set 등을 활용한 효율적인 대기열 관리 가능 
  - Sorted Set을 사용하여 대기열을 구현하면, `O(log(N))` 시간 복잡도로 대기열 위치를 조회할 수 있다.

<br>

### 3.2 주요 변경 사항
#### 3.2.1 데이터 모델 변경

- Queue 엔티티 대신 Redis의 Sorted Set 사용
  - Sorted Set은 Redis의 데이터 구조 중 하나로, 각 요소가 score와 연관되어 있는 정렬된 집합이다. 
  - 이를 통해 대기열의 순서를 효율적으로 관리할 수 있습니다.


- 활성화 키: ACTIVE_KEY, 대기열 키: WAIT_KEY
  - ACTIVE_KEY 키는 조회 시 Active 상태를 반환하고 다음 요청으로 넘어갈 수 있도록 한다.
  - WAIT_KEY 키는 현재 대기 중인 사용자들의 정보를 저장한다.
  - 각 키는 Sorted Set으로 구현되어 있어 효율적인 순서 관리가 가능하다.

#### 3.2.2 저장소 계층 변경
- Redis Template을 사용한 데이터 접근 로직 구현

```Java
    @Bean
    public RedisTemplate<String, Queue> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Queue> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Queue.class));
        return redisTemplate;
    }
```


#### 3.2.3 비즈니스 로직 변경

- `QueueService`의 로직을 Redis 기반으로 재구현
```Java
@Service
@RequiredArgsConstructor
@Slf4j
public class QueueServiceImpl implements QueueService {

    private final RedisTemplate<String, Queue> redisTemplate;

    private static final long EXPIRATION_TIME = 10;
    private static final int MAX_ACTIVE_TOKENS = 300;
    private static final String WAIT_KEY = "WAIT_KEY";
    private static final String ACTIVE_KEY = "ACTIVE_KEY";

    @Override
    public QueueResponseDto addToQueue(Long userId, Long concertScheduleId) {
        try {
            deleteTokens(userId);

            long currentTime = System.currentTimeMillis();

            Long activeTokenCount = redisTemplate.opsForZSet().size(ACTIVE_KEY);
            activeTokenCount = (activeTokenCount != null) ? activeTokenCount : 0L;

            Queue queue = Queue.builder()
                    .userId(userId)
                    .concertScheduleId(concertScheduleId)
                    .requestTime(currentTime)
                    .build();

            if (activeTokenCount < MAX_ACTIVE_TOKENS) {
                redisTemplate.opsForZSet().add(ACTIVE_KEY, queue, currentTime);

                redisTemplate.expire(ACTIVE_KEY, EXPIRATION_TIME, TimeUnit.MINUTES);

                return QueueResponseDto.builder()
                        .concertScheduleId(concertScheduleId)
                        .status("active")
                        .build();
            } else {
                redisTemplate.opsForZSet().add(WAIT_KEY, queue, currentTime);

                redisTemplate.expire(WAIT_KEY, EXPIRATION_TIME, TimeUnit.MINUTES);

                Long waitingOrder = redisTemplate.opsForZSet().rank(WAIT_KEY, queue);
                waitingOrder = (waitingOrder != null) ? waitingOrder : -1L;
                long waitTime = (waitingOrder + 1) * 1000;

                return QueueResponseDto.builder()
                        .concertScheduleId(concertScheduleId)
                        .status("waiting")
                        .waitingOrder(waitingOrder + 1)
                        .remainingTime(waitTime / 1000)
                        .build();
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.QUEUE_ERROR);
        }
    }

    private Queue findTokenInQueue(Long userId, String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForZSet().range(key, 0, -1))
                    .orElse(Set.of())
                    .stream()
                    .filter(token -> token.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REDIS_ERROR);
        }
    }
}

```

#### QueueService의 주요 변경 사항 및 개선점:

- 실시간 대기열 위치 계산:
  - 기존: 복잡한 SQL 쿼리를 통해 대기열 위치를 계산한다.
  - 개선: `redisTemplate.opsForZSet()`의 `rank()` 메서드를 사용하여 O(log(N)) 시간 복잡도로 즉시 위치 계산이 가능하다.


- 대기열 상태 관리:
  - 기존: 데이터베이스 트랜잭션을 통한 상태 변경한다.
  - 개선: Redis의 원자적 연산을 통해 즉각적인 상태 변경 및 반영이 가능하다.


- 토큰 발급 및 대기열 등록:
  - 기존: 데이터베이스 INSERT 연산으로 토큰을 발급하고 대기열을 등록한다.
  - 개선: `redisTemplate.opsForZSet()`의 `add()` 메서드를 사용하여 빠르게 대기열을 등록한다.


- 대기 시간 예측:
  - Redis 로 변경하며 새로 추가된 기능으로, 현재 위치와 처리 속도를 기반으로 예상 대기 시간을 계산한다.


- 만료된 대기열 처리:
  - 기존: 주기적인 데이터베이스 쿼리를 통해 만료처리한다.
  - 개선: TTL을 통해 유효기간을 설정해 자동으로 삭제된다.

<br>

###  3.3 구현 과정 및 고려사항
#### 3.3.1 Redis 구성

- Redis 설정 클래스 (RedisConfig) 구현:
  - 이 클래스는 Redis 연결 및 데이터 직렬화/역직렬화 방식을 정의한다.
- Redis Template 빈 설정:
  - RedisTemplate은 Redis 작업을 추상화하여 개발자가 더 쉽게 Redis 작업을 수행할 수 있게 해준다.

```Java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Queue> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Queue> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Queue.class));
        return redisTemplate;
    }
}
```
- `StringRedisSerializer()`와 `GenericJackson2JsonRedisSerializer()`를 사용하여 키와 값의 효율적인 직렬화/역직렬화를 보장했다.
  - `StringRedisSerializer()`는 키를 문자열로 저장하여 Redis에서 효율적으로 검색할 수 있게 한다. 
  - `Jackson2JsonRedisSerializer<>()`는 복잡한 객체를 JSON 형식으로 저장하여 데이터의 구조를 유지하면서도 효율적인 저장과 검색을 가능하게 한다.

<br>

#### 3.3.2 데이터 구조 설계
Redis의 Sorted Set을 사용한 대기열 구현은 다음과 같은 이점을 제공한다:

- 효율적인 순서 관리: Sorted Set은 O(log N) 시간 복잡도로 요소 추가, 제거, 순위 조회가 가능하다.
- 시간 기반 정렬: 시스템 현재 시간을 score로 사용함으로써, 선입선출(FIFO) 방식의 대기열을 자연스럽게 구현할 수 있다. 즉, 선착순으로 구현이 가능하다.
- 범위 쿼리 효율성: 특정 시간 범위의 항목을 효율적으로 조회하거나 제거할 수 있다.

구조:
```Text
키: ACTIVE_KEY, WAIT_KEY
값: "queue_1": {
      "userId": 123,
      "concertScheduleId": 456,
      "requestTime": 1675224545000
    }
스코어: "requestTime"(밀리초)
```
- 이러한 구조를 통해 대기열의 순서, 처리 상태, 만료 시간 등을 효과적으로 관리할 수 있다고 기대했다.

<br>
    
#### 3.3.3 대기열 관리 로직 구현

```Java
    private Queue findTokenInQueue(Long userId, String key) {
        try {
            log.debug("Searching for token in queue. UserId: {}, Key: {}", userId, key);
            return Optional.ofNullable(redisTemplate.opsForZSet().range(key, 0, -1))
                    .orElse(Set.of())
                    .stream()
                    .filter(token -> token.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error occurred while searching for token in queue. UserId: {}, Key: {}", userId, key, e);
            throw new CustomException(ErrorCode.REDIS_ERROR);
        }
    }

    @Override
    public QueueResponseDto getQueueStatus(Long userId, Long concertScheduleId) {
        try {
            log.debug("getQueueStatus called for userId: {}, concertScheduleId: {}", userId, concertScheduleId);

            Queue token = findTokenInQueue(userId, ACTIVE_KEY);
            if (token != null) {
                redisTemplate.opsForZSet().remove(ACTIVE_KEY, token);
                log.info("User found in ACTIVE_KEY. Removing from ACTIVE_KEY. UserId: {}", userId);
                return QueueResponseDto.builder()
                        .concertScheduleId(concertScheduleId)
                        .status("active")
                        .build();
            }

            token = findTokenInQueue(userId, WAIT_KEY);
            if (token != null) {
                Long waitingOrder = redisTemplate.opsForZSet().rank(WAIT_KEY, token);
                waitingOrder = (waitingOrder != null) ? waitingOrder : -1L;
                long waitTime = ((waitingOrder / MAX_ACTIVE_TOKENS) * 10);

                log.info("User found in WAIT_KEY. Waiting order: {}, Estimated wait time: {} seconds", waitingOrder + 1, waitTime);
                return QueueResponseDto.builder()
                        .concertScheduleId(concertScheduleId)
                        .status("waiting")
                        .waitingOrder(waitingOrder + 1)
                        .remainingTime(waitTime)
                        .build();
            }

            log.warn("Token not found for userId: {} in both WAIT_KEY and ACTIVE_KEY", userId);
            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while retrieving queue status for userId: {}, concertScheduleId: {}", userId, concertScheduleId, e);
            throw new CustomException(ErrorCode.QUEUE_ERROR);
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void activateTokens() {
        try {
            log.info("Activating tokens...");

            long currentTime = System.currentTimeMillis();
            log.info("Current time: {}", currentTime);

            Set<Queue> tokensToActivate = Optional.ofNullable(
                    redisTemplate.opsForZSet().range(WAIT_KEY, 0, MAX_ACTIVE_TOKENS - 1)
            ).orElse(Set.of());

            log.info("Tokens to activate from WAIT_KEY: {}", tokensToActivate.size());

            for (Queue token : tokensToActivate) {
                log.info("Activating token {} from WAIT_KEY to ACTIVE_KEY...", token);
                redisTemplate.opsForZSet().remove(WAIT_KEY, token);
                redisTemplate.opsForZSet().add(ACTIVE_KEY, token, token.getRequestTime());
            }

            log.info("Token activation process completed.");
        } catch (Exception e) {
            log.error("Error during token activation process: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.ACTIVATE_ERROR);
        }
    }

    public void deleteTokens(Long userId) {
        try {
            log.debug("Deleting tokens for userId: {}", userId);

            Queue waitToken = findTokenInQueue(userId, WAIT_KEY);
            if (waitToken != null) {
                redisTemplate.opsForZSet().remove(WAIT_KEY, waitToken);
                log.info("Token removed from WAIT_KEY. UserId: {}", userId);
            }

            Queue activeToken = findTokenInQueue(userId, ACTIVE_KEY);
            if (activeToken != null) {
                redisTemplate.opsForZSet().remove(ACTIVE_KEY, activeToken);
                log.info("Token removed from ACTIVE_KEY. UserId: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error occurred while deleting tokens for userId: {}", userId, e);
            throw new CustomException(ErrorCode.REDIS_ERROR);
        }
    }
```

`addToQueue()`

- 목적: 새로운 사용자를 대기열에 추가
- 작동 방식:
    - 현재 `ACTIVE_KEY`의 수가 200개 미만일 경우 `ACTIVE_KEY`로 키를 설정하여 바로 좌석 페이지로 접속할 수 있도록 함
    - 현재 `ACTIVE_KEY`의 수가 200개 이상일 경우 `WAIT_KEY`로 키를 설정하여 차례대로 `ACTIVE_KEY`가 될 때까지 대기하게 함
    -`redisTemplate.opsForZSet()`의 `add()` 메서드를 사용하여 O(log N) 시간 복잡도로 추가
- 이점: 대규모 동시 접속 상황에서도 빠른 대기열 등록 가능


`findTokenInQueue()`

- 목적: 특정 사용자의 토큰 조회
- 작동 방식: key를 매개변수로 받아 필요한 key 내에서만 효율적인 검색
- 이점: 사용자별 중복 대기열 등록 방지 및 대기열 삭제에 활용


`deleteTokens()`

- 목적: 대기열에서 특정 항목 제거
- 작동 방식: `redisTemplate.opsForZSet()`의 `remove()` 메서드 사용
- 이점: 대기열 페이지 이탈 등 토큰 취소 시 즉각적인 대기열 정리 가능


`activateTokens()`

- 목적: 대기 상태에서 활성화 상태로 전환
- 작동 방식:
    - `MAX_ACTIVE_TOKENS`에서 현재 `ACTIVE_KEY`의 갯수를 뺀만큼의 `WAIT_KEY`의 데이터를 `ACTIVE_KEY`로 전환
    - 수정이 안되는 Redis 특성 상 같은 데이터를 가진 `ACTIVE_KEY`를 만든 후 `WAIT_KEY` 삭제
- 이점: 동시성 문제 없이 안전한 상태 전환 보장

`getQueueStatus()`

- 목적: 특정 토큰의 상태 조회
- 작동 방식:
    -`ACTIVE_KEY`인 경우 Active 상태를 담아 유저에게 반환
    -`WAIT_KEY`인 경우 Wait 상태와 함께 현재 대기 순번과 예상 시간을 같이 반환
    - `redisTemplate.opsForZSet()`의 `rank()` 메서드 사용 (O(log N) 시간 복잡도)
- 이점: 대규모 대기열에서도 빠른 위치 조회 가능

<br>

#### 3.3.4 성능 최적화

- 대기 시간 예측 로직 구현 (calculateEstimatedWaitSeconds)

```Java
    Long waitingOrder = redisTemplate.opsForZSet().rank(WAIT_KEY, token);
    waitingOrder = (waitingOrder != null) ? waitingOrder : -1L;
    long waitTime = ((waitingOrder / MAX_ACTIVE_TOKENS) * 10);

```
- 수치 산출의 가정과 근거
  - DB에 동시에 접근할 수 있는 트래픽의 최대치 : 1초에 약 1,000TPS ⇒ 1분에 60,000건의 트랜잭션 처리 가능
  - 1명의 유저가 좌석 조회부터 결제까지 걸리는 시간 : 평균 4분
  - 1분간 유저가 호출하는 API 횟수 : 3(좌석조회, 좌석예약, 결제처리) * 2(동시성 이슈에 의해 예약실패하여 API 재처리) = 6
  - 1분당 처리할 수 있는 동시 접속자 수 : 2500명
  - 10초에 약 416명의 접속자를 처리가능하다고 예상되나 특정한 시간대에 한꺼번에 많은 요청이 들어올 경우 트랜잭션이 골고루 분산되지 못하는 경우를 고려해 보수적으로 계산
  - 선형 확장성: 대기열 크기에 따라 처리 시간이 선형적으로 증가한다고 가정

- 수치 산출의 한계
  - 부하 테스트를 진행하였으나 현재 배포가 진행되지 않은 단계이고 로컬환경에서의 테스트이므로 정확한 서버의 한계치를 측정하지 못함.
  - 따라서 위 로직은 실제 서버의 처리 능력과 부하 상황에 따라 조정이 필요할 수 있다. 
  - 정확한 예측을 위해서는 실제 운영 데이터를 기반으로 한 지속적인 모니터링과 조정이 필요하다.

#### 3.3.5 동시성 제어

- Redis의 원자적 연산을 활용한 동시성 관리:
  - Redis는 단일 스레드 모델을 사용하여 명령어를 순차적으로 처리한다.
  - 예를 들어, ZADD 명령어는 원자적으로 실행되어 동시에 여러 클라이언트가 같은 키에 접근하더라도 데이터 일관성이 보장된다.
  - 이를 통해 RDB 와 같은 별도의 락(lock) 메커니즘 없이도 안전한 동시성 제어가 가능하다.

- 사용자별 중복 대기열 등록 방지 로직:
  - `findTokenInQueue()` 메서드를 사용하여 사용자가 이미 대기열에 있는지 확인한다.
  - 기존 토큰이 있다면 제거하고 새로운 토큰을 발급함으로써 중복 등록을 방지한다.
  - 이 과정은 Redis의 트랜잭션을 활용하여 원자적으로 수행될 수 있다.

<br>

#### 3.3.6 확장성 고려

- 처리 중인 대기열 수 제한 설정 (ALLOWED_PROCESSING_TOKEN_COUNT_LIMIT)
  - 시스템의 처리 능력에 맞춰 동시에 처리할 수 있는 요청의 수를 제한한다.
  - 이를 통해 시스템 과부하를 방지하고 안정적인 서비스 제공이 가능하다.
  - 한계 : 위에서 언급하였듯, 부하 테스트 없이 임의의 대기열 수 제한을 설정한 것이므로, 보다 정확한 수치 산정은 부하 테스트 이후 가능하다.
<br>

## 4> 주요 개선 사항
### 4.1 성능 향상

- 대기열 위치 조회 성능 대폭 개선 (O(log N) 복잡도)
  - 근거: 
    - Redis의 Sorted Set 자료구조를 활용하여 대기열을 구현했다. 
    - Sorted Set은 요소의 검색, 삽입, 삭제 연산이 모두 O(log N) 시간 복잡도를 가진다.
    - 기존 관계형 데이터베이스에서는 대기열 위치 조회를 위해 COUNT 쿼리를 사용해야 했으며, 이는 O(N) 시간 복잡도를 가진다.
    - 예를 들어, 100만 명의 대기열에서 위치를 조회할 때, 기존 방식은 최악의 경우 100만 번의 연산이 필요했지만, Redis를 사용한 새 방식은 약 20번의 연산으로 위치를 찾을 수 있다. (log2(1,000,000) ≈ 20)


- 실시간 대기열 상태 업데이트 가능
  - 근거: 
    - Redis의 인메모리 특성과 단일 스레드 모델을 활용하여 실시간 업데이트를 구현했다.
    - Redis의 ZADD, ZREM 등의 명령어는 원자적으로 실행되어 데이터 일관성을 보장하면서도 밀리초 단위의 빠른 응답 시간을 제공한다.


### 4.2 확장성 증가

- 대규모 동시 접속 처리 가능
  - Redis의 초당 수만 건의 연산 처리 능력을 활용하여 대규모 동시 접속 상황에서도 안정적인 서비스가 가능하다.
  - Redis Cluster를 통해 수평적 확장이 용이하여, 트래픽 증가에 따라 유연하게 시스템을 확장할 수 있다.


- 유연한 대기열 관리 (쉬운 확장 및 축소)
  - Redis의 Sorted Set을 사용하여 대기열을 구현함으로써, 대기열의 크기를 동적으로 조절할 수 있다.
  - 예를 들어, 대기열 용량을 10만에서 100만으로 늘리는 데 별도의 스키마 변경이나 시스템 중단 없이 즉시 적용이 가능하다.

### 4.3 실시간성 확보

- 대기열 상태 변경 즉시 반영
  - Redis의 인메모리 특성으로 인해 디스크 I/O가 최소화되어, 상태 변경이 즉시 반영된다.
  - 예를 들어, 사용자가 대기열에 진입하거나 빠져나갈 때 밀리초 단위의 지연으로 전체 대기열 상태가 업데이트된다.

- 실시간 대기 시간 예측 기능 구현
  - 현재 대기열 위치와 처리 속도를 기반으로 한 로직을 통해 실시간으로 대기 시간을 예측합니다.
  - 이를 통해 사용자에게 더 정확한 예상 대기 시간 정보를 제공할 수 있고, 좋은 사용자 경험을 줄 수 있다.
