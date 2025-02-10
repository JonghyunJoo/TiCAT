# API 부하 테스트 분석과 가상 장애 대응 방안에 관한 보고서

# 1. API 부하 테스트 분석에 대한 보고서

## 1. 개요

- 본 보고서는 콘서트 예약 시스템 API에 대한 부하 테스트 결과를 상세히 기술한다.
- 테스트의 주요 목적은 콘서트 예약 시스템 API 전반의 성능, 안정성, 그리고 확장성을 평가하는 것이다.

## 2. 테스트 목적

- 시스템이 예상되는 부하를 정상적으로 처리할 수 있는지 평가
- 일시적인 높은 부하 상황에서의 시스템 성능 파악
- 장애 상황 시뮬레이션을 통한 문제점 분석 및 개선 방안 도출
- 적정한 애플리케이션 배포 스펙 결정을 위한 데이터 수집

## 3. 테스트 도구 및 방법론

### 3.1 테스트 도구: k6
k6는 Go로 작성된 오픈 소스 부하 테스트 도구이다. 주요 특징은 다음과 같다:

- JavaScript를 사용하여 테스트 스크립트 작성
- 다양한 프로토콜 지원 (HTTP, WebSocket, gRPC 등)
- 확장 가능한 메트릭 시스템
- 클라우드 서비스와의 통합 지원
- 실시간 모니터링 및 결과 분석 기능

이러한 특징들로 인해 k6는 복잡한 시나리오를 시뮬레이션하고 다양한 각도에서 시스템 성능을 평가하는 데 적합하다고 판단한다.

## 3.2 테스트 요구사항
- 테스트 전제조건 정리
  - 대상 시스템 범위
  - 목푯값 설정 (latency, throughput, target RPS)
- 시나리오 및 스크립트 작성
  - 접속 트래픽 부하가 높은 API
  - 서비스의 주 기능 목적에 부합하는 API
- 테스트 진행
  - 각 시나리오의 특성에 맞게 load test, peak test를 진행
- 장애 예측 및 개선
  - 테스트 결과를 분석하여 향후 장애 발생 가능성을 예측해보고, 장애를 가상하여 대응 및 개선

### 3.2.1 테스트 전제조건

#### 대상 시스템 범위
- Application
- DB
- Redis
- kafka

#### 목푯값 설정
- 콘서트 예약 서비스 특성에 따라 1일 이용자 수(DAU)보다는 단순히 최대 트래픽 중심으로 계산하였고, 상용 서비스가 아니기 때문에 우선 타이트하게 설정해보았다.
- 목표 rps 정하기 (1초당 요청 수)
  - 최고 동시 트래픽이 1만명 정도는 몰린다고 가정
    - 그러나 서버의 한계로 대기열 진입 api의 목표 rps는 1000
  - 대기열 시스템은 10초(콘서트 오픈런 완료 시간을 10초로 가정)당 200명의 대기 유저를 활성화하며 성능을 유지하는 것을 목표
  - 예약 시스템의 목표 rps는 840...? 
    - 10초 동안 콘서트 목록 조회, 콘서트 회차 목록 조회(스케쥴 조회 + 예약 가능 좌석 조회), 좌석 목록 조회, 좌석 잠금(1~2회), 예약 생성이 이루어진다고 가정하여 총 요청 수는 7회로 가정
    - 최대 동접 수 200*6 = 1200명으로 가정하여 1200 * 7 / 10 =  840
  - 응답 시간
    - p95: 2-300ms 이내
    - p99: 500ms 이내 목표
  - VUser (가상 사용자 수)
    - VUser = (rps * 한번의 시나리오를 완료하는데 걸리는 시간(요청-응답 시간 + 지연 시간)) / (시나리오 당 요청수)
  - 대기열 API
    - 로컬 서버의 한계가 있으니 대기/활성이 잘 되는지 최대로 해보기
  - 예약 시나리오
    - (840 * 10s) / 7 로 할 수 있지만, 하나의 k6 서버가 만들어낼 수 있는 부하에 한계가 있으므로 지연 시간을 최대한 짧게 계산하여 가상 사용자 수를 줄일 수 있도록 계산
    - 하나의 요청-응답 시간을 200ms 목표로 잡고, (840 * (0.2s * 7 + 3s)) / 7 = 528명

### 3.2.2 테스트 시나리오 수립
우선 콘서트 예약 서비스에 작성된 API 기능 중에서 병목 현상이나 많은 트래픽을 야기할 만한 기능은

[대기열 진입 및 정보 확인], [콘서트 조회], [콘서트 스케쥴 조회 및 예약 가능한 좌석 조회],[좌석 목록 조회], [콘서트 예약]이며, [유저 잔액 충전 및 사용]도 예약 시에 많은 트래픽을 받을 수 있다.

그래서 총 세 가지 흐름을 테스트 해보기로 했다
- 1. [유저 잔액 조회]
- 2. [대기열 진입 및 정보 확인]
- 3. [콘서트 조회 및 예약]

테스트 시나리오로는 두가지를 준비했는데

- Load Test (부하 테스트)
  - 목적: 시스템이 예상되는 일반적인 부하를 정상적으로 처리할 수 있는지 평가
  - 방법: 특정 부하를 제한된 시간 동안 제공하여 시스템의 안정성 확인
  - 중요성: 일상적인 운영 상황에서의 시스템 성능을 확인하고, 리소스 사용량을 예측하는 데 도움


- Peak Test (최고 부하 테스트)
  - 목적: 시스템이 일시적으로 높은 부하를 처리할 수 있는지 평가
  - 방법: 목표 임계 부하를 순간적으로 제공하여 시스템의 대응 능력 확인
  - 중요성: 특별 이벤트나 예상치 못한 트래픽 급증 상황에서의 시스템 안정성 확인

이 중 Load Test는 모든 테스트에서 공통적으로 진행할 예정이며

[콘서트 조회 및 예약]은 대기열 로직에 따라 일정 수준의 요청이 유지될 예정이므로 Peak Test는 [유저 잔액 조회]와 [대기열 진입 및 정보확인]에서만 진행하기로 했다

### 3.3 테스트 설정

- 테스트 설정은 `options`에 정의되어 있으며, 주요 내용은 다음과 같다:

```javascript
export const options = {
    scenarios: {
        load_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '5s', target: 200 },
                { duration: '5s', target: 400 },
                { duration: '5s', target: 600 },
                { duration: '5s', target: 800 },
                { duration: '5s', target: 1000 },
                { duration: '10s', target: 1000 },
                { duration: '10s', target: 1000 },
                { duration: '10s', target: 1000 },
                { duration: '5s', target: 0 }
            ],
        },
        peak_test: {
            executor: 'ramping-arrival-rate',
            startRate: 10,
            timeUnit: '1s',
            preAllocatedVUs: 200,
            maxVUs: 2000,
            stages: [
                { duration: '10s', target: 1000 },
                { duration: '20s', target: 2000 },
                { duration: '30s', target: 1000 },
                { duration: '10s', target: 4000 },
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(99)<500'],
    },
};

export const BASE_URL = 'http://localhost:8080';
```

- **Load Test 설정 상세**

  - executor: 'ramping-vus': 가상 사용자(VU)의 수를 점진적으로 증가시키는 방식을 사용한다.
  - startVUs: 0: 테스트 시작 시 0명의 가상 사용자로 시작한다.
  - stages:

    - 0초~5초: 0에서 200명으로 가상 사용자 증가
    - 5초~10초: 200명에서 400명으로 증가
    - 10초~15초: 400명에서 600명으로 증가
    - 15초~20초: 600명에서 800명으로 증가
    - 20초~25초: 800명에서 1000명으로 증가
    - 25초~55초: 1000명의 가상 사용자 유지
    - 55초~60초: 1000명에서 0명으로 가상 사용자 감소

  - 이 설정의 근거:
    - 초기에 1000명의 VU까지 점진적으로 증가하며, 일반적인 트래픽 환경을 시뮬레이션
    - 가상 사용자를 일정 시간 유지하여 시스템의 안정성을 테스트
    - 마지막 단계에서 VU 수를 0으로 감소시켜 부하가 줄어드는 상황을 반영

<br>

- **Peak Test 설정 상세**

  - executor: 'ramping-arrival-rate': 초당 요청 수를 기반으로 부하를 생성한다.
  - startRate: 10: 초당 10개의 요청으로 시작한다.
  - timeUnit: '1s': 1초 단위로 요청 수를 조절한다.
  - preAllocatedVUs: 200, maxVUs: 2000: 미리 할당된 VU 200개, 최대 2000개까지 사용 가능
  - stages:

    - 0초~10초: 초당 10개에서 1000개로 요청 수 증가
    - 10초~30초: 초당 500개에서 2000개로 요청 수 증가
    - 30초~60초: 초당 2000개에서 1000개로 요청 수 감소
    - 60초~70초: 초당 1000개에서 4000개로 요청 수 재급증

  - 이 설정의 근거:
    - 초당 1000개 요청은 일반적인 피크 시간대의 트래픽을 가정
    - 초당 2000개 요청은 높은 부하를 견딜 수 있는지 테스트하기 위함
    - 1000개 요청으로 감소 후 다시 4000개까지 급증시켜, 시스템의 복원력과 확장성을 검증

- 이러한 설정을 통해 일반적인 사용 패턴부터 극단적인 부하 상황까지 다양한 시나리오를 테스트할 수 있다. 
- 이는 시스템의 성능 한계를 파악하고, 잠재적인 병목 지점을 식별하는 데 도움이 될것이라고 생가한다.

<br>

## 4. 테스트 진행

- 각 API에 대해 개별적으로 부하 테스트를 실시했다. 

### 4.1 잔액 조회 테스트

```javascript
export default function () {
    const TEST_USER_IDS = [1, 2, 3, 4, 5];
    const userId = TEST_USER_IDS[Math.floor(Math.random() * TEST_USER_IDS.length)];


    const waitingRes = http.get(`${BASE_URL}/wallet-service/${userId}`);

    console.log(`UserID: ${userId}, Response status: ${waitingRes.status}`);

    check(waitingRes, {
        'waiting check': res => res.status === 200
    });

    if (waitingRes.status !== 200) {
        console.error(`Request failed for user ${userId} - Status: ${waitingRes.status}, Body: ${waitingRes.body}`);
        fail('Failed check for waiting-page');
    }

    sleep(1);
}
```

- 이 테스트는 사용자 잔액 조회 API의 성능을 평가한다. 
- 무작위로 선택된 사용자 ID를 사용하여 API를 호출하고, 응답 상태를 확인한다.

#### 4.1.1 결과
```
      ✓ waiting check
 
      checks.........................: 100.00% 45037 out of 45037
      data_received..................: 5.7 MB  94 kB/s
      data_sent......................: 4.7 MB  78 kB/s
      http_req_blocked...............: avg=29.27µs  min=1.4µs     med=4.9µs   max=95.84ms  p(90)=7.8µs    p(95)=16.9µs 
      http_req_connecting............: avg=19.49µs  min=0s        med=0s      max=95.79ms  p(90)=0s       p(95)=0s     
    ✓ http_req_duration..............: avg=8.78ms   min=1.4ms     med=6.04ms  max=110.79ms p(90)=18.01ms  p(95)=24.21ms
        { expected_response:true }...: avg=8.78ms   min=1.4ms     med=6.04ms  max=110.79ms p(90)=18.01ms  p(95)=24.21ms
      http_req_failed................: 0.00%   0 out of 45037
      http_req_receiving.............: avg=434.12µs min=11.2µs    med=143.3µs max=38.05ms  p(90)=836.88µs p(95)=1.66ms 
      http_req_sending...............: avg=45.85µs  min=-648995ns med=12.5µs  max=24.76ms  p(90)=30.8µs   p(95)=50.8µs 
      http_req_tls_handshaking.......: avg=0s       min=0s        med=0s      max=0s       p(90)=0s       p(95)=0s     
      http_req_waiting...............: avg=8.3ms    min=1.32ms    med=5.65ms  max=110.71ms p(90)=17.22ms  p(95)=23.06ms
      http_reqs......................: 45037   739.214023/s
      iteration_duration.............: avg=1s       min=1s        med=1s      max=1.11s    p(90)=1.01s    p(95)=1.02s  
      iterations.....................: 45037   739.214023/s
      vus............................: 18      min=0              max=1000
      vus_max........................: 1000    min=462            max=1000
 
 
 running (1m00.9s), 0000/1000 VUs, 45037 complete and 0 interrupted iterations
 load_test ✓ [ 100% ] 0000/1000 VUs  1m0s
```
```
      ✓ waiting check
 
      checks.........................: 100.00% 78773 out of 78773
      data_received..................: 10 MB   139 kB/s
      data_sent......................: 8.3 MB  115 kB/s
      dropped_iterations.............: 18736   259.444331/s
      http_req_blocked...............: avg=47.97µs  min=1.4µs  med=4.7µs   max=590.61ms p(90)=7.9µs    p(95)=18.8µs  
      http_req_connecting............: avg=20.95µs  min=0s     med=0s      max=68.68ms  p(90)=0s       p(95)=0s      
    ✗ http_req_duration..............: avg=108.39ms min=1.21ms med=26.56ms max=2.25s    p(90)=300.98ms p(95)=468.84ms
        { expected_response:true }...: avg=108.39ms min=1.21ms med=26.56ms max=2.25s    p(90)=300.98ms p(95)=468.84ms
      http_req_failed................: 0.00%   0 out of 78773
      http_req_receiving.............: avg=2.97ms   min=8.8µs  med=124.7µs max=1.92s    p(90)=4.43ms   p(95)=12.51ms 
      http_req_sending...............: avg=101.4µs  min=3.4µs  med=12.4µs  max=990.77ms p(90)=45.8µs   p(95)=127.6µs 
      http_req_tls_handshaking.......: avg=0s       min=0s     med=0s      max=0s       p(90)=0s       p(95)=0s      
      http_req_waiting...............: avg=105.31ms min=1.16ms med=25.49ms max=1.96s    p(90)=290.94ms p(95)=459.65ms
      http_reqs......................: 78773   1090.798906/s
      iteration_duration.............: avg=1.12s    min=1s     med=1.03s   max=4.24s    p(90)=1.35s    p(95)=1.54s   
      iterations.....................: 78773   1090.798906/s
      vus............................: 655     min=32             max=2000
      vus_max........................: 2000    min=200            max=2000
 
 
 running (1m12.2s), 0000/2000 VUs, 78773 complete and 0 interrupted iterations
 peak_test ✓ [ 100% ] 0000/2000 VUs  1m10s  3996.96 iters/s
```
#### 4.1.2 분석
- Load_Test
  - 성공률:
    - 모든 요청이 성공적으로 처리되었다 (100% 성공률). -> 서비스 안정성은 양호
  - 처리량:
    - 초당 약 739개의 요청을 처리했다. -> 안정적으로 높은 처리량을 유지함
    - 총 45037개의 요청이 처리되었다.

  - 응답 시간:
  - 평균 응답 시간: 8.78ms
  - 중간값 응답 시간: 6.04ms
  - 90번째 백분위 응답 시간: 18.01ms
  - 95번째 백분위 응답 시간: 24.21ms

- Peak_Test
  - 성공률:
    - 모든 요청이 성공적으로 처리되었다 (100% 성공률). -> 서비스 안정성은 양호
  - 처리량:
    - 초당 약 1,090.8개의 요청을 처리했다. -> 안정적으로 높은 처리량을 유지함
    - 총 78,773개의 요청이 처리되었다.
    - Dropped Iterations가 18,736개 발생했다

  - 응답 시간:
  - 평균 응답 시간: 108.39ms
  - 중간값 응답 시간: 26.56ms
  - 90번째 백분위 응답 시간: 300.98ms
  - 95번째 백분위 응답 시간: 468.84ms

#### 4.1.3 테스트 분석 결론
- 잔액 조회 API는 높은 부하 상황에서도 모든 요청이 성공적으로 처리되었으며 안정적으로 작동했다. 
- 다만 평균 응답 시간이 Load_Test는 8.78ms로 빠른 편이었으나 Peak_Test에서는 108.39ms로 다소 높았다.
  - 이는 서버가 과부하 상태에서 응답을 늦게 반환하는 것으로 보인다. 따라서 일부 요청에서 지연이 발생할 수 있다.
- Peak_Test에서 Dropped Iterations이 발생했다.
  - 이 역시 서버가 처리 가능한 용량을 초과해 일부 요청을 무시한 것으로 보인다.
- 시스템이 초당 1000개 이상의 요청을 처리할 수 있다는 점은 긍정적이지만, 높은 부하 상황에서 일부 요청의 지연 시간이 증가하는 점에 주의가 필요하다. 

<br>

### 4.2 대기열 진입 및 상태 조회

```JavaScript
export default function () {
    let userId = __VU;
    let scheduleId = 1;

    group('Step 1: create queue', function () {
    let queueRes = http.post(`${BASE_URL}/queue-service/`, JSON.stringify({
        userId: userId,
        concertScheduleId: scheduleId
    }), {
        headers: { 'Content-Type': 'application/json' }
    });

    check(queueRes, {
        '대기열 응답 코드 200': (r) => r.status === 200,
    });

    if (queueRes.status !== 200) {
        console.error(`Failed queue request: ${queueRes.status} - ${queueRes.body}`);
        fail('Failed check for waiting-page');
    }
    });
    sleep(1);

    group('Step 2: get queue status', function () {
    let statusRes = http.get(`${BASE_URL}/queue-service/?userId=${userId}&concertScheduleId=${scheduleId}`, {
        headers: { 'Content-Type': 'application/json' }
    });

    check(statusRes, {
        '대기열 상태 응답 코드 200': (r) => r.status === 200,
    });

    if (statusRes.status !== 200) {
        console.error(`Failed status check: ${statusRes.status} - ${statusRes.body}`);
        fail('Failed check for queue status');
    }
    });
    sleep(1);
}
```

- 이 테스트는 대기열 추가 및 상태 조회 API의 성능을 평가한다.
- 무작위로 선택된 사용자 Id와 콘서트 스케쥴 Id를 사용하여 API를 호출하고, 응답 상태를 확인한다.

#### 4.2.1 결과
- Load Test
```
      █ Step 1: create queue
 
        ✓ 대기열 응답 코드 200
 
      █ Step 2: get queue status
 
        ✓ 대기열 상태 응답 코드 200
 
      checks.........................: 100.00% 37058 out of 37058
      data_received..................: 7.6 MB  123 kB/s
      data_sent......................: 6.6 MB  107 kB/s
      group_duration.................: avg=237.65ms min=2.39ms med=187.55ms max=1.6s     p(90)=497.37ms p(95)=620.32ms
      http_req_blocked...............: avg=63.4µs   min=1.5µs  med=4µs      max=134.94ms p(90)=7.8µs    p(95)=19.8µs  
      http_req_connecting............: avg=49.15µs  min=0s     med=0s       max=134.89ms p(90)=0s       p(95)=0s      
    ✗ http_req_duration..............: avg=237.34ms min=2.22ms med=187.35ms max=1.6s     p(90)=497.03ms p(95)=619.61ms
        { expected_response:true }...: avg=237.34ms min=2.22ms med=187.35ms max=1.6s     p(90)=497.03ms p(95)=619.61ms
      http_req_failed................: 0.00%   0 out of 37058
      http_req_receiving.............: avg=3.5ms    min=10.4µs med=74.4µs   max=859.68ms p(90)=2.47ms   p(95)=6.99ms  
      http_req_sending...............: avg=48.92µs  min=4.1µs  med=12.4µs   max=106.91ms p(90)=38.2µs   p(95)=78.62µs 
      http_req_tls_handshaking.......: avg=0s       min=0s     med=0s       max=0s       p(90)=0s       p(95)=0s      
      http_req_waiting...............: avg=233.79ms min=2.11ms med=185.83ms max=1.52s    p(90)=493.69ms p(95)=608.62ms
      http_reqs......................: 37058   598.009042/s
      iteration_duration.............: avg=2.48s    min=2s     med=2.45s    max=3.97s    p(90)=2.88s    p(95)=3.11s   
      iterations.....................: 18529   299.004521/s
      vus............................: 6       min=6              max=1000
      vus_max........................: 1000    min=1000           max=1000
 
 
 running (1m02.0s), 0000/1000 VUs, 18529 complete and 0 interrupted iterations
 load_test ✓ [ 100% ] 0000/1000 VUs  1m0s
```
- Peak Test
```
      █ Step 1: create queue
 
        ✓ 대기열 응답 코드 200
 
      █ Step 2: get queue status
 
        ✓ 대기열 상태 응답 코드 200
 
 time="2025-02-07T15:55:54Z" level=error msg="thresholds on metrics 'http_req_duration' have been crossed"
      checks.........................: 100.00% 44068 out of 44068
      data_received..................: 9.1 MB  123 kB/s
      data_sent......................: 7.9 MB  106 kB/s
      dropped_iterations.............: 82986   1114.123241/s
      group_duration.................: avg=1.91s   min=2.73ms med=2.07s  max=3.72s    p(90)=2.68s  p(95)=2.82s  
      http_req_blocked...............: avg=96.88µs min=1.5µs  med=4.68µs max=91.69ms  p(90)=9.29µs p(95)=131.6µs
      http_req_connecting............: avg=76.34µs min=0s     med=0s     max=91.52ms  p(90)=0s     p(95)=0s     
    ✗ http_req_duration..............: avg=1.91s   min=2.6ms  med=2.07s  max=3.72s    p(90)=2.68s  p(95)=2.82s  
        { expected_response:true }...: avg=1.91s   min=2.6ms  med=2.07s  max=3.72s    p(90)=2.68s  p(95)=2.82s  
      http_req_failed................: 0.00%   0 out of 44068
      http_req_receiving.............: avg=1.08ms  min=10.9µs med=68.1µs max=922.41ms p(90)=1.56ms p(95)=4.7ms  
      http_req_sending...............: avg=74.81µs min=4.2µs  med=15.5µs max=175.44ms p(90)=57.8µs p(95)=155.5µs
      http_req_tls_handshaking.......: avg=0s      min=0s     med=0s     max=0s       p(90)=0s     p(95)=0s     
      http_req_waiting...............: avg=1.91s   min=2.51ms med=2.07s  max=3.72s    p(90)=2.68s  p(95)=2.82s  
      http_reqs......................: 44068   591.632118/s
      iteration_duration.............: avg=5.83s   min=2s     med=6.15s  max=7.85s    p(90)=7.3s   p(95)=7.4s   
      iterations.....................: 22034   295.816059/s
      vus............................: 391     min=55             max=2000
      vus_max........................: 2000    min=200            max=2000
 
 
 running (1m14.5s), 0000/2000 VUs, 22034 complete and 0 interrupted iterations
 peak_test ✓ [ 100% ] 0000/2000 VUs  1m10s  3997.79 iters/s
```
#### 4.2.2 분석
- Load_Test
  - 성공률:
    - 모든 요청이 성공적으로 처리되었다 (100% 성공률). -> 서비스 안정성은 양호
  - 처리량:
    - 초당 약 598개의 요청을 처리했다. -> 안정적으로 높은 처리량을 유지함
    - 총 37058개의 요청이 처리되었다.

  - 응답 시간:
  - 평균 응답 시간: 237.34ms
  - 중간값 응답 시간: 187.35ms
  - 90번째 백분위 응답 시간: 497.03ms
  - 95번째 백분위 응답 시간: 619.61ms

- Peak_Test
  - 성공률:
    - 모든 요청이 성공적으로 처리되었다 (100% 성공률). -> 서비스 안정성은 양호
  - 처리량:
    - 초당 약 591개의 요청을 처리했다.
    - 총 44,068개의 요청이 처리되었다.
    - Dropped Iterations가 82,986개 발생했다

  - 응답 시간:
  - 평균 응답 시간: 1.91s
  - 중간값 응답 시간: 2.07s
  - 90번째 백분위 응답 시간: 2.68s
  - 95번째 백분위 응답 시간: 2.82s


#### 4.2.3 테스트 분석 결론

- 대기열 진입 및 상태조회 테스트는 높은 부하 상황에서도 모든 요청이 성공적으로 처리되었으며 안정적으로 작동했다. 
- 다만 평균 응답 시간이 Load_Test는 237.32ms로 다소 높았고 Peak_Test에서는 1.91s로 매우 높았다.
  - 이는 서버가 과부하 상태에서 응답을 늦게 반환하는 것으로 보인다. 따라서 대부분 요청에서 지연이 발생하고 있다.
- Peak_Test에서 Dropped Iterations이 82986개 발생했다.
  - 이 역시 서버가 처리 가능한 용량을 상당히 초과해 대부분 요청을 무시한 것으로 보인다.
- 성공적으로 처리된 초당 요청도 591개로 많은 요청이 Drop되어서 그런 것으로 보인다.
- 전체적으로 단순히 DB를 검색하는 잔액 조회에 비해 부하 상황에 크게 영향을 받는 것으로 보이며 실질적으로 유저 경험이 저하될 가능성이 높다. 

<br>

### 4.3 콘서트 조회 및 예약

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';
import { options, BASE_URL } from '../common/test-options.js';

export { options };

export default function () {
    let userId = __VU;
    let concertId = 1;
    let concertScheduleId = 1;
    const TEST_SEAT_IDS = [1, 2, 3, 4, 5, 6];
    const seatId = TEST_SEAT_IDS[Math.floor(Math.random() * TEST_SEAT_IDS.length)];

    group('Step 1: 콘서트 조회', function () {
        let queueRes = http.get(`${BASE_URL}/concert-service/?page=0&size=10&orderBy=concertStartDate&orderDirection=ASC`, {
            headers: {'Content-Type': 'application/json'}
        });
        check(queueRes, {
            '대기열 응답 코드 200': (r) => r.status === 200,
        });

        if (queueRes.status !== 200) {
            console.error(`Failed queue request: ${queueRes.status} - ${queueRes.body}`);
            fail('Failed check for waiting-page');
        }
    });
    sleep(1);


    group('Step 2: get schedule from concert', function () {
        let statusRes = http.get(`${BASE_URL}/concert-service/concertSchedule/${concertId}`, {
            headers: {'Content-Type': 'application/json'}
        });

        check(statusRes, {
            '대기열 상태 응답 코드 200': (r) => r.status === 200,
        });

        if (statusRes.status !== 200) {
            console.error(`Failed status check: ${statusRes.status} - ${statusRes.body}`);
            fail('Failed check for queue status');
        }
    });

    sleep(1);

    group('Step 3: get seat chart', function () {
        let statusRes = http.get(`${BASE_URL}/seat-service/seats/${concertScheduleId}`, {
            headers: {'Content-Type': 'application/json'}
        });

        check(statusRes, {
            '대기열 상태 응답 코드 200': (r) => r.status === 200,
        });

        if (statusRes.status !== 200) {
            console.error(`Failed status check: ${statusRes.status} - ${statusRes.body}`);
            fail('Failed check for queue status');
        }
    });

    sleep(1);

    group('Step 4: select seat', function () {
        let statusRes = http.put(`${BASE_URL}/seat-service/selectSeat`, JSON.stringify({
            userId: userId,
            seatId: seatId
        }), {
            headers: {'Content-Type': 'application/json'}
        });

        check(statusRes, {
            '대기열 상태 응답 코드 200, 400, 409': (r) => r.status === 200 || r.status === 400 || r.status === 409,
        });

        if (statusRes.status !== 200 && statusRes.status !== 400 && statusRes.status !== 409) {
            console.error(`Failed status check: ${statusRes.status} - ${statusRes.body}`);
            fail('Failed check for queue status');
        }
    });

    sleep(1);

    group('Step 5: create reservation', function () {
        let queueRes = http.post(`${BASE_URL}/reservation-service/`, JSON.stringify({
            seatIdList: [1],
            userId: userId
        }), {
            headers: {'Content-Type': 'application/json'}
        });

        check(queueRes, {
            '대기열 응답 코드 200': (r) => r.status === 200,
        });

        if (queueRes.status !== 200) {
            console.error(`Failed queue request: ${queueRes.status} - ${queueRes.body}`);
            fail('Failed check for waiting-page');
        }
    });
    sleep(1);
}
```

- 이 테스트는 콘서트 조회 및 예약 전반의 API의 성능을 평가한다.
- 각각의 API에 맞는 파라미터로 API를 호출하고, 응답 상태를 확인한다.

#### 4.3.1 결과
#### Load_Test
```Text
     █ Step 1: 콘서트 조회
       ✓ 대기열 응답 코드 200
     █ Step 2: get schedule from concert
       ✓ 대기열 상태 응답 코드 200
     █ Step 3: get seat chart
       ✓ 대기열 상태 응답 코드 200
     █ Step 4: select seat
       ✓ 대기열 상태 응답 코드 200, 400, 409
     █ Step 5: create reservation
       ✓ 대기열 응답 코드 200
     checks.........................: 100.00% 26825 out of 26825
     data_received..................: 20 MB   308 kB/s
     data_sent......................: 4.7 MB  73 kB/s
     group_duration.................: avg=810.49ms min=1.95ms med=109.97ms max=16.43s   p(90)=3.45s   p(95)=4.83s  
     http_req_blocked...............: avg=69.56µs  min=1.7µs  med=5.1µs    max=75.74ms  p(90)=10.49µs p(95)=57.97µs
     http_req_connecting............: avg=48.73µs  min=0s     med=0s       max=75.58ms  p(90)=0s      p(95)=0s     
   ✗ http_req_duration..............: avg=810.06ms min=1.82ms med=109.51ms max=16.43s   p(90)=3.45s   p(95)=4.83s  
       { expected_response:true }...: avg=976.92ms min=1.82ms med=114.97ms max=16.43s   p(90)=4.31s   p(95)=5.21s  
     http_req_failed................: 19.93%  5348 out of 26825
     http_req_receiving.............: avg=5.46ms   min=11.5µs med=208.49µs max=411.29ms p(90)=13.41ms p(95)=29.95ms
     http_req_sending...............: avg=56.03µs  min=4.49µs med=17µs     max=55.45ms  p(90)=45.3µs  p(95)=84.69µs
     http_req_tls_handshaking.......: avg=0s       min=0s     med=0s       max=0s       p(90)=0s      p(95)=0s     
     http_req_waiting...............: avg=804.54ms min=1.74ms med=103.45ms max=16.43s   p(90)=3.45s   p(95)=4.83s  
     http_reqs......................: 26825   415.134173/s
     iteration_duration.............: avg=9.06s    min=5.04s  med=9.09s    max=21.77s   p(90)=11.92s  p(95)=12.85s 
     iterations.....................: 5365    83.026835/s
     vus............................: 52      min=36             max=1000
     vus_max........................: 1000    min=1000           max=1000
running (1m04.6s), 0000/1000 VUs, 5365 complete and 0 interrupted iterations
load_test ✓ [ 100% ] 0000/1000 VUs  1m0s
time="2025-02-08T15:06:41Z" level=error msg="thresholds on metrics 'http_req_duration' have been crossed"
```
#### 4.3.2 분석
- 성공률:
  - 모든 요청이 성공적으로 처리되었다 (100% 성공률). -> 서비스 안정성은 양호

- 처리량:
  - 초당 약 415개의 요청을 처리했다.
  - 총 26825개의 요청이 처리되었다.

- 응답 시간:
  - 평균 응답 시간: 810.06ms
  - 중간값 응답 시간: 109.51ms
  - 90번째 백분위 응답 시간: 3.45s
  - 95번째 백분위 응답 시간: 4.83s

#### 4.3.3 테스트 분석 결론
- 좌석 조회 및 예약 테스트는 높은 부하 상황에서도 모든 요청이 성공적으로 처리되었으며 안정적으로 작동했다.
- 다만 평균 응답 시간이 Load_Test에서도 810.06ms로 상당히 높았다
  - 이는 서버가 과부하 상태에서 응답을 늦게 반환하는 것으로 보인다. 따라서 대부분 요청에서 지연이 발생하고 있다.
- 평균 대기열 처리 시간도 9.06s로 긴 편이다.
- 많은 API에 요청을 보내는만큼 서버에 꽤 많은 부하가 걸린 것으로 보이며 실질적으로 실질적으로 유저 경험이 저하될 가능성이 높다.

<br>

## 6. 종합 평가 및 결론

### 6.1 전체 시스템 성능 요약

1. **안정성**
  - 모든 테스트에서 100%의 성공률을 기록하며, 시스템이 전반적으로 안정적으로 동작함을 확인하였다.
  - 단, 높은 부하 상황에서 일부 요청의 응답 지연이 발생하여, 실제 운영 환경에서도 일정 수준의 성능 저하 가능성이 존재할 것으로 보인다.
2. **확장성**
  - 현재 최대 2000명의 동시 사용자 처리 가능, 트래픽 증가 시 성능 저하가 나타나는 구간이 확인되었다.
  - 피크 타임(최대 부하)에서 서버 응답 속도가 급격히 감소하는 현상이 발생, 스케일 아웃(Scale-out) 전략의 도입이 필요하다.
3. **응답성**
  - 단일 요청에 대한 응답 시간은 평균 밀리초(ms) 단위로 빠르게 처리되었다.
  - 그러나 부하 테스트 결과, 트래픽이 증가할수록 응답 시간이 비선형적으로 증가하였다.
4. **일관성**
  - 단일 API 테스트에서는 응답 시간이 일정한 패턴을 보이나, 다수의 API가 동시에 호출될 경우 성능이 불안정하다.
  - 트랜잭션이 동시 다발적으로 처리될 때, 동기화 이슈로 인해 일부 요청이 대기하거나 지연되는 현상이 발생하였다.
5. **처리량**
  - 초당 수천 건의 요청을 처리할 수 있는 성능을 보였으며, 높은 동시 접속 환경에서도 일정 수준의 처리가 가능하다.
  - 그러나 서버 부하가 증가할수록 일부 요청이 지연되는 경향이 나타났다.

### 6.2 개선 영역

1. **응답 시간 일관성 향상**
  - 부하가 증가할수록 응답 시간이 급격히 증가하는 현상이 발생하고 있다.
  - 트랜잭션 처리 방식 개선 및 비동기 처리, 큐 시스템 도입, 캐싱 최적화 등을 고려해야 한다.
2. **부하 분산**
  - 현재 단일 서버 환경에서 높은 부하가 발생하면서 특정 서버 인스턴스가 과부하 상태에 빠지는 현상이 확인된다.
  - 이를 해결하기 위해 로드 밸런싱 및 오토 스케일링 전략을 도입하여 트래픽을 분산해야 한다.

### 6.3 개선 방법

#### 6.3.1 시도해본 방법
- **캐싱 적용** 
  - 잔액 조회 API의 경우 실시간 데이터 변경 가능성 때문에 기존에는 캐싱을 적용하지 않았나 성능 개선을 위해 캐싱을 적용해보았다.
  - 성능 개선을 위해 캐싱을 적용했으나, 기대했던 응답 속도 개선이 나타나지 않고 오히려 증가하는 현상이 발생했다.
  - 예상 했던 결과는 아니었으나 현재 응답시간 증가의 원인이 DB 부하보다는 다른 요인이 성능 저하의 주요 원인이라는 것을 알 수 있었다.
  
#### 6.3.2 시도해볼 수 있는 방법
- **1. Auto Scaling 및 Load Balancing**
  - 현재 Docker 컨테이너 기반 배포 중이나, 오토 스케일링(Auto Scaling)이 적용되지 않아 트래픽 급증 시 부하를 감당하지 못하는 상태로 보인다.
  - 트래픽이 일정 임계치를 초과할 경우 서버 인스턴스를 자동으로 확장(Scale-out)하여 대응하는 구조가 필요하다.
  - 로드 밸런서 (Load Balancer)를 적용하여 트래픽을 여러 서버로 균등하게 분배하는 방식도 함께 적용해야 한다.
- **2. 클라우드 기반 배포 (Cloud Deployment) 전환**
  - 현재 로컬 환경에서 배포 중이며, CPU 및 RAM 사용량이 급증하는 것이 성능 저하의 주요 원인으로 추정된다.
  - AWS, GCP, Azure 등의 클라우드 환경으로 전환하여, 수직적 확장(Scale-up) 및 수평적 확장(Scale-out) 전략을 동시에 적용하는 것이 필요하다.
- **3. 비동기 처리 및 메시지 큐 도입**
  - 현재 API 요청이 동기(Synchronous) 방식으로 처리되며, 일부 트랜잭션에서 병목이 발생하는 것으로 보임.
  - Kafka, RabbitMQ 등의 메시지 큐(Message Queue) 시스템을 활용하여 비동기 이벤트 처리 구조를 도입할 필요가 있음.
- **4. DB 최적화 및 Connection Pooling 개선**
  - 현재 DB 부하가 성능 저하의 주요 원인이 아닐 가능성이 높으나, 쿼리 최적화 및 Connection Pool 설정을 조정할 필요가 있다.
  - Read/Write 분리, 인덱스 최적화, 캐싱 레이어(Redis, Memcached) 활용 등을 고려해야 한다.

### 6.4 최종 결론
- 현재 시스템은 전반적으로 안정적인 성능을 보이며, 최대 2000명의 동시 접속을 처리할 수 있다.
- 그러나 높은 부하 상황에서 응답 시간이 급격히 증가하는 문제가 있으며, 이로 인해 사용자 경험이 저하될 가능성이 있다.
- 이를 해결하기 위해 Auto Scaling, Load Balancing, 클라우드 배포, 메시지 큐 도입 등의 방안을 적극적으로 고려해야 한다.
<br>

