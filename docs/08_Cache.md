# 캐시 도입을 통한 성능 개선 보고서

## 1. 개요
- 본 보고서는 콘서트 조회 및 콘서트 스케쥴 조회에 Cache 를 도입한 내용을 분석하는 것으로 한다.
- 주요 목적은 Cache 를 도입한 이유와 분석, 그리고 도입한 과정과 결과를 설명하는 것이다.
- 대규모 트래픽이 예상되는 콘서트 예약 시스템에서 Cache 도입은 데이터베이스 부하 감소와 응답 시간 단축을 위한 핵심 전략이다.

<br>

## 2. 캐시 설정 분석

### 2.1 캐시 설정 코드 분석
```Java
@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory());

        return redisTemplate;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory){
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> redisCacheConfigurationMap = new HashMap<>();
        redisCacheConfigurationMap.put("concertCache", redisCacheConfiguration.entryTtl(Duration.ofMinutes(5)));
        redisCacheConfigurationMap.put("concertScheduleCache", redisCacheConfiguration.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory)
                .cacheDefaults(redisCacheConfiguration)
                .withInitialCacheConfigurations(redisCacheConfigurationMap)
                .disableCachingNullValues()
                .build();
    }
}
```

- `@EnableCaching` 어노테이션을 사용하여 Spring의 캐싱 기능을 활성화한다. 이는 Spring Framework에 캐시 관련 기능을 자동으로 구성하도록 지시한다.

- `RedisCacheManager` Bean 을 정의하여 Redis 캐시 매니저를 커스터마이즈한다. 
  - `entryTtl(Duration.ofMinute(ttlMin))`: 캐시 엔트리의 TTL을 설정한다.
  - `serializeKeysWith(StringRedisSerializer())`: 캐시 키를 문자열로 직렬화한다.
  - `serializeValuesWith(GenericJackson2JsonRedisSerializer(objectMapper))`: 캐시 값을 JSON 형식으로 직렬화한다.

<br>

### 2.2 캐시 설정의 합리성

#### TTL 설정:
- 데이터의 특성에 따라 5분의 TTL을 설정함으로써, 데이터의 정합성과 캐시의 효율성을 균형있게 조절했다.
- 근거: 처음엔 ConcertSchedule의 availableSeats가 수시로 변경될 것을 고려해 `concertScheduleCache`에는 더 짧은 TTL(1분)을 설정했으나 availableSeats를 feignClient를 통해 호출하는 로직으로 바꾸면서 캐시에 저장되지 않게 되었기 때문에 5분의 TTL이 Concert와 ConcertSchedule 모두에게 적당하다고 판단했다. 

#### Null 값 캐싱 방지:

- `disableCachingNullValues()`를 통해 불필요한 null 값의 캐싱을 방지하여 캐시 공간을 효율적으로 사용하도록 했다.
- 근거: null 값을 캐시하는 것은 불필요한 메모리 사용을 초래하고, 잠재적으로 의미 있는 데이터가 캐시되는 것을 방해할 수 있다. 또한, null 값을 반환하는 쿼리의 경우 캐시 없이 직접 데이터베이스에 접근하는 것이 더 효율적일 수 있다.

#### 직렬화 설정:

- `StringRedisSerializer`와 `GenericJackson2JsonRedisSerializer`를 사용하여 키와 값의 효율적인 직렬화/역직렬화를 보장했다.
- 근거: 
  - `StringRedisSerializer`는 키를 문자열로 저장하여 Redis에서 효율적으로 검색할 수 있게 한다. 
  - `GenericJackson2JsonRedisSerializer`는 복잡한 객체를 JSON 형식으로 저장하여 데이터의 구조를 유지하면서도 효율적인 저장과 검색을 가능하게 한다.
  - 이러한 직렬화 전략은 데이터의 효율적인 저장과 빠른 검색을 가능하게 하여 캐시의 성능을 최적화한다.


이러한 캐시 설정은 대규모 트래픽이 예상되는 콘서트 예약 시스템에서 데이터베이스 부하를 줄이고 응답 시간을 단축시키는 데 크게 기여할 것으로 예상된다. 

<br>

## 3. 캐시 적용 분석

### 3.1 ConcertService의 캐시 적용

#### 3.1.1 getConcertsByConditions() 메소드
```Java
    @Override
    @Cacheable(
            value = "concertCache",
            key = "{#concertRequestDto.title, #concertRequestDto.searchStartDate, #concertRequestDto.searchEndDate}",
            unless = "#result == null || #result.content.isEmpty()",
    )
    public ConcertPageDto<ConcertResponseDto> getConcertsByConditions(ConcertSearchRequestDto concertRequestDto) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        try {
            Sort sort = Sort.by(concertRequestDto.getOrderBy());
            if (concertRequestDto.getOrderDirection().equalsIgnoreCase("DESC")) {
                sort = sort.descending();
            } else {
                sort = sort.ascending();
            }

            Pageable pageable = PageRequest.of(concertRequestDto.getPage(), concertRequestDto.getSize(), sort);

            Specification<Concert> spec = (root, query, criteriaBuilder) -> null;

            if (concertRequestDto.getTitle() != null) {
                spec = spec.and(ConcertSpecification.likeTitle(concertRequestDto.getTitle()));
            }


            if (concertRequestDto.getSearchStartDate() != null) {
                startDateTime = startTimeParser(concertRequestDto.getSearchStartDate());
                if (concertRequestDto.getSearchEndDate() != null) {
                    endDateTime = endTimeParser(concertRequestDto.getSearchEndDate());
                } else {
                    endDateTime = startDateTime.plusMonths(1);
                }
                spec = spec.and(ConcertSpecification.betweenStartDateAndEndDate(startDateTime, endDateTime));
            }

            Page<Concert> concertPage = concertRepository.findAll(spec, pageable);
            return new ConcertPageDto<>(concertPage.map(concert -> modelMapper.map(concert, ConcertResponseDto.class)));
        } catch (IllegalArgumentException ex) {
            log.error("공연 검색 조건이 잘못되었습니다.", ex);
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        } catch (DataAccessException ex) {
            log.error("조건별 공연 조회 중 데이터베이스 오류 발생", ex);
            throw new CustomException(ErrorCode.DATABASE_ERROR);
        }
    }
```

- 캐시 키: `#concertRequestDto.title`, `#concertRequestDto.searchStartDate`, `#concertRequestDto.searchEndDate`
- TTL: 5분
  - 근거: 
    - 콘서트 목록은 콘서트 스케쥴을 조회하는 것에 비해 상대적으로 변경 빈도가 낮은 데이터로 판단했다. 
    - 5분의 TTL을 사용함으로써 데이터의 일관성을 유지하면서도 캐시의 효과를 극대화할 수 있다.
    - 콘서트의 변경(추가 및 삭제)가 긴급하게 적용되어야 할 필요가 없다고 생각해 5분의 지연은 허용 가능한 수준으로 판단했다.
    - 보다 긴 TTL은 데이터베이스 쿼리 횟수를 더욱 줄여 시스템 성능을 향상시킬 수 있다.
- 조건: `#result` 혹은 `#result.content`가 null이 아닐 때만 캐시 적용
- 동기화: sync = true로 설정하여 동시 요청 시 중복 연산 방지
- 특히 JPA Specification을 활용하여 쿼리가 길고 복잡할 것으로 예상되는 `getConcertsByConditions()`인 만큼 캐싱이 유효할 것으로 판단했다.

#### 3.1.2 getSchedulesByConcertId() 메소드

```Java
    @Override
    @Cacheable(
            value = "concertScheduleCache",
            key = "#concertId",
            unless = "#result == null || #result.isEmpty()",
    )
    public List<ConcertScheduleResponseDto> getSchedulesByConcertId(Long concertId) {
        List<ConcertSchedule> concertSchedules = concertScheduleRepository.findByConcertId(concertId);

        return concertSchedules.stream()
                .map(schedule -> {
                    ConcertScheduleResponseDto responseDto = modelMapper.map(schedule, ConcertScheduleResponseDto.class);
                    Long availableSeats = seatClient.getAvailableSeatsForSchedules(schedule.getId()).getAvailableSeats();
                    responseDto.setAvailableSeats(availableSeats);
                    return responseDto;
                })
                .collect(Collectors.toList());
    }
```

- 캐시 키: `#concertId`
- TTL: 5분
  - 근거:
    - 콘서트 스케줄 상세 정보는 예약 상황에 따라 가용 좌석이 빈번하게 변경될 수 있기 때문에 짧은 TTL이 필요하다고 판단했다.
    - 그러나 그 이상으로 실시간성이 필요하다고 보았고 캐싱을 더 짧은 TTL로 갱신하기엔 리소스적으로 부적합하다고 판단했다.
    - 때문에 availableSeats는 불러올 때마다 새로 가져오도록 Seat Service로 분리하고 ConcertSchedule에도 동일하게 5분의 TTL을 부여했다.
- 조건: #result가 모두 null이 아닐 때만 캐시 적용
- 동기화: sync = true로 설정하여 동시 요청 시 중복 연산 방지

### 3.2 캐시 적용의 합리성

#### 빈번한 조회 데이터 캐싱
- 콘서트 목록과 콘서트 별 스케줄은 자주 조회되는 데이터다.
- 이러한 데이터를 캐싱함으로써 반복적인 데이터베이스 쿼리를 줄여 전체적인 시스템 성능을 향상시킬 수 있다.

#### 조건부 캐싱
- result와 result.content의 데이터를 확인하여 불필요한 캐싱을 방지했다.
- 이는 무의미한 데이터가 캐시를 차지하는 것을 방지하고, 캐시 공간을 효율적으로 사용할 수 있는 것을 기대했다.

#### 동기화 설정
- sync = true 설정으로 동시에 여러 요청이 들어올 경우, 한 번만 데이터베이스에 접근하여 캐시를 생성하도록 했다.
- 이는 동시성 문제를 해결하고, 불필요한 데이터베이스 쿼리를 방지하여 시스템의 안정성과 효율성을 높이는 것을 기대했다.
<br>

## 4. 성능 개선 효과 분석
### 4.1 쿼리 최적화

- 중복 쿼리 감소: 캐시를 통해 동일한 데이터에 대한 반복적인 데이터베이스 쿼리를 크게 줄일 수 있다.
- 복잡한 쿼리 결과 캐싱: `getConcertsByConditions()`와 같은 복잡한 쿼리 결과를 캐싱함으로써, 데이터베이스의 부하를 크게 감소시킬 수 있다.

예를 들어, `getConcertsByConditions()` 메서드 실행 시 다음과 같은 쿼리가 실행될 수 있다:
```sql
SELECT c.*
FROM concert c
WHERE (c.title LIKE '%:title%' OR :title IS NULL)
  AND (c.start_date BETWEEN :startDate AND :endDate OR :startDate IS NULL OR :endDate IS NULL)
ORDER BY c.:orderBy :orderDirection
LIMIT :size OFFSET :page * :size;
```

#### 캐시 사용으로 인한 개선의 이득
- 이러한 복잡한 쿼리는 캐시 적용 후 캐시 히트 시 실행되지 않아, 데이터베이스 부하를 크게 줄일 수 있다고 생각한다. 
- 특히 대량의 동시 접속이 예상되는 콘서트 예약 시스템에서, 이러한 캐싱 전략은 데이터베이스의 부하를 크게 줄이고 전체 시스템의 응답 시간을 개선하는 데 중요한 역할을 한다고 생각한다.

### 4.2 대량 트래픽 처리 능력 향상

- 응답 시간 단축: 
  - 캐시된 데이터를 사용함으로써, 데이터베이스 조회 시간을 크게 줄여 전체적인 응답 시간을 단축시킬 수 있다.
  - 예: 콘서트별 스케쥴 조회 쿼리 시 758 ms가 소요되던 요청이 캐시 히트 시 11ms로 크게 단축되었다.

- 데이터베이스 부하 분산:
  - 캐시를 통해 데이터베이스로의 직접적인 요청을 줄임으로써, 대량의 트래픽 발생 시에도 데이터베이스의 부하를 효과적으로 분산시킬 수 있다.
  - 예: 초당 1000건의 요청 중 80%가 캐시에서 처리된다면, 데이터베이스는 초당 200건의 요청만 처리하면 된다.
<br>

## 5. 콘서트 조회와 콘서트 스케쥴 기능에 Cache 도입 결론
본 프로젝트에서 구현한 캐시 전략은 대량의 트래픽 발생 시 발생할 수 있는 지연 문제를 효과적으로 해결할 수 있는 방안이라고 생각한다.

- 데이터베이스 부하 감소: 반복적인 쿼리를 캐시로 대체하여 데이터베이스 부하를 크게 줄인다.
- 응답 시간 단축: 복잡한 쿼리 결과를 캐시에서 즉시 제공하여 응답 시간을 대폭 단축시킨다.
- 데이터 일관성 유지: 적절한 TTL 설정과 상태 변경 시 즉시 캐시 무효화를 통해 데이터의 일관성을 유지한다.

<br>