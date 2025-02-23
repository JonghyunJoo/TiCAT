# 🎫 대규모 트래픽에도 안정적인 콘서트 티켓팅 플랫폼


- [프로젝트 개요](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#-%EC%84%9C%EB%B9%84%EC%8A%A4-%EA%B0%9C%EC%9A%94)

- [목표 & KPI](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#-%EB%AA%A9%ED%91%9C--kpi%ED%95%B5%EC%8B%AC%EC%84%B1%EA%B3%BC%EC%A7%80%ED%91%9C)

- [주요 기술](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#-%EC%A3%BC%EC%9A%94-%EA%B8%B0%EC%88%A0)

  - [MSA](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#-msa)
    - msa 설계도
    - msa를 도입하며 얻은 이점
    - Spring Cloud Gateway 및 Spring Eureka

  - [배포 및 CI/CD (25.02~)](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#-%EB%B0%B0%ED%8F%AC-%EB%B0%8F-cicd-2502-)
    - 아키텍처 설계도
    - 배포 과정
    - 배포 환경의 변화로 얻은 이점

  - [동시성 문제와 극복](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#--%EB%8F%99%EC%8B%9C%EC%84%B1-%EB%AC%B8%EC%A0%9C%EC%99%80-%EA%B7%B9%EB%B3%B5)
  - [대기열 시스템 설계 및 Redis 이관](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#-%EB%8C%80%EA%B8%B0%EC%97%B4-%EC%8B%9C%EC%8A%A4%ED%85%9C-%EC%84%A4%EA%B3%84-%EB%B0%8F-redis-%EC%9D%B4%EA%B4%80)
  - [캐시 도입을 통한 성능 개선](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#--%EC%BA%90%EC%8B%9C-%EB%8F%84%EC%9E%85%EC%9D%84-%ED%86%B5%ED%95%9C-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0)
  - [쿼리 분석 및 인덱스 필요성 평가](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#--%EC%BF%BC%EB%A6%AC-%EB%B6%84%EC%84%9D-%EB%B0%8F-%EC%9D%B8%EB%8D%B1%EC%8A%A4-%ED%95%84%EC%9A%94%EC%84%B1-%ED%8F%89%EA%B0%80)
  - [API 부하 테스트 분석과 장애 대응 방안](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#-api-%EB%B6%80%ED%95%98-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EB%B6%84%EC%84%9D%EA%B3%BC-%EC%9E%A5%EC%95%A0-%EB%8C%80%EC%9D%91-%EB%B0%A9%EC%95%88)

- [기술 스택](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#-%EA%B8%B0%EC%88%A0-%EC%8A%A4%ED%83%9D)
- [프로젝트 산출물](https://github.com/JonghyunJoo/ConcertReservation?tab=readme-ov-file#-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%82%B0%EC%B6%9C%EB%AC%BC)

<br>

---

# ✨ 서비스 개요

**특정한 시간에 사람들이 한꺼번에 몰려 트래픽이 집중되는 것을 가정한 티케팅 사이트**

**대규모 트래픽이 있는 상황에서도 안정적으로 요청을 처리할 수 있는 아키텍처를 고민해보고 프로젝트에 적용**

<br> 

---
# 🎯 목표 & KPI(핵심성과지표)

### 1. 안정적인 예약 및 결제 시스템 구축

- 대규모 트래픽 상황에서도 서버 안정성 유지
  - **예약 시스템 가용성 99.9% 이상 유지**
  - **TPS(초당 트랜잭션) 500 이상 처리 가능** (부하 테스트 기준)
  - 예약 및 결제 프로세스의 평균 응답 시간 **1초 이내** 유지


- 대기열을 통한 서버 부하 방지 및 서비스 안정성 확보
  - 동시 접속자 **1만명 이상** 처리 목표
  - 대기열 진입 후 평균 대기 시간 3분 **이내**

### 2. 공정한 예약 시스템 제공
- 중복 예약 방지
  - 동시성 제어를 통한 **중복 예약 발생율 0% 목표**


- 예매 실패자를 위한 취소표 알림 시스템 운영
  - 대기열 혹은 동시성 제어로 인해 예매하지 못한 유저 대상으로 **취소표 알림 기능 제공**

### 3. 모니터링 및 장애 대응 체계 구축

- 애플리케이션 및 DB 성능 모니터링
  - 시스템 리소스(CPU, 메모리, DB 연결 수) **실시간 모니터링 대시보드 제공**
  - 특정 임계치 초과 시 **자동 알람 및 확장(스케일링) 기능 활성화**

### 가정
- 짧은 시간에 많은 트래픽이 발생할 것으로 가정
- 특정 요청에 많은 트래픽이 동시에 발생할 것으로 가정

<br>

---
# 👨‍💻 주요 기술
## ✔ MSA
MSA란 MicroService Architecture의 약자로, 기존의 Monolithic Architecture의 한계를 벗어나 애플리케이션을 느슨하게 결합된 서비스의 모임으로 구조화하는 서비스 지향 아키텍처(SOA) 스타일의 일종인 소프트웨어 개발 기법이다.

기존에 진행하고 있던 콘서트 예약 프로젝트를 크게 6개의 서비스를 분리하고 서버와 DB를 각 서비스에 맞게 분류하여 구현하였다.

### MSA 설계도
![Image](https://github.com/user-attachments/assets/b65db890-1750-4ab3-bc1b-927a5e24e999)

### MSA를 도입하며 얻은 이점
#### 확장성
- 필요에 따라 **수평적 확장**(scale-out)이 가능하다.
- **클라우드 기반 서비스 사용**에 적합하다.
#### 유연성
- 서비스 단위의 독립적인 수정과 배포가 가능해 **운영의 유연성**이 확보된다.
- 서비스별로 적합한 **기술 스택을 자유롭게 선택**가능하다.
#### 장애 대응성
- 하나의 서비스에 발생한 장애가 전체 시스템에 영향을 미치지 않아 **서비스 가용성**이 확보된다.
- **부분 장애 발생 시 빠른 대응**이 가능하고 **장애 발생 후 회복력**이 높아진다.

### 더 자세한 MSA 전환 과정이 궁금하시다면

#### 🔗 [Docs 5. kafka_transactional](https://github.com/JonghyunJoo/Spring_Cloud_ConcertReservation/blob/master/docs/05_kafka_transactional.md)

#### 🔗 [MSA 전환기 포스트](https://velog.io/@j3261221/MSA-MSA-전환-프로젝트-MSA란)


## ✔ 배포 및 CI/CD (25.02 ~)
기존 로컬환경에서 Docker Desktop을 통해 배포하던 방식에서 Google Kubernetes Engine을 통해 구축한 쿠버네티스 클러스터에 배포하는 방식으로 변경
### 아키텍처
![Image](https://github.com/user-attachments/assets/45458f90-1d6e-4f40-b250-60d0fa4c73d6)

### 배포과정
![Image](https://github.com/user-attachments/assets/8d480f1c-a3f6-4918-8d1f-712a59d822cb)

### 배포 환경 변화의 이점
#### 자동화 및 효율성
- Jenkins와 ArgoCD를 사용하여 CI/CD 파이프라인과 배포 프로세스를 자동화하고, Kubernetes와 Helm을 통해 애플리케이션을 쉽게 배포 및 관리가 가능해졌다.
#### 확장성
- 트래픽에 따라 클러스터를 자동으로 확장하거나 축소할 수 있게 되어 시스템의 확장성이 뛰어나다.
- 오토 스케일링
  - HPA.yaml에 미리 지정해둔 CPU 사용량을 초과하면 Pod의 수를 증가시켜 부하를 분산시킨다.
#### 유지 관리 용이성
- ArgoCD와 GitOps를 통해 애플리케이션의 상태를 Git에서 관리하면서, 버전 관리와 롤백을 쉽게 할 수 있다.
#### 배포 용이성
- Helm을 통해 애플리케이션 배포를 표준화하고, 다양한 환경에 맞는 설정을 쉽게 적용할 수 있다.
- Jenkins의 자동화된 빌드 및 테스트, ArgoCD의 GitOps 방식, Kubernetes의 안정적이고 자동화된 배포 메커니즘을 통해 배포 오류를 최소화할 수 있다.
- 롤링 업데이트를 통한 무중단 배포가 가능하다.
  ![Image](https://github.com/user-attachments/assets/b17e8860-e605-4909-8fb2-291d1095e2e3)

## ✔  동시성 문제와 극복
### 동시성 문제
좌석 예약이나 잔액 충전과 같이 여러 사용자가 동시에 요청할 때, 순차적으로 처리해야 하는 요청에서 동일한 DB 항목에 대해 중복된 요청이 처리되는 문제가 발생하였다.
### 해결책
#### 좌석 예약 기능
이중 락 전략(분산 락 + 비관적 락)을 활용해 Redis 분산락으로 1차 동시성 제어를 수행하고 비관적 락으로 2차 안정장치를 마련해 데이터 정합성을 보장했다.
#### 잔액 충전
분산 락만을 활용해 여러번의 충전 요청 중 한 번만 성공하도록 하여 우발적인 중복 충전을 방지하였다.
### 결과
- 분산 락과 비관적 락의 조합을 통해 대규모 동시 요청 상황에서도 데이터 정합성 유지라는 목적을 달성했다.
- Redis를 사용한 분산 락으로 빠른 응답시간을 유지하면서 비관적 락의 2차 안전장치를 통해 데이터베이스 수준의 안정성을 보장했다.
#### 🔗[Docs 6. ConcurrencyReport](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/06_ConcurrencyReport.md)

## ✔ 대기열 시스템 설계 및 Redis 이관
많은 수의 이용자가 한꺼번에 요청을 시도할 것으로 예상되는 서비스들에 대해 안정적인 서비스 제공을 위해 대기열 시스템이 필요하다고 판단하였다.
### Redis의 Sorted Set을 활용한 대기열 모델로 변경
#### Sorted Set의 이점
- **효율적인 순서 관리** : Sorted Set은 O(log N) 시간 복잡도로 요소 추가, 제거, 순위 조회가 가능하다.
- **시간 기반 정렬**: 시스템 현재 시간을 score로 사용함으로써, 선입선출(FIFO) 방식의 대기열을 자연스럽게 구현할 수 있다.
- **범위 쿼리 효율성**: 특정 시간 범위의 항목을 효율적으로 조회하거나 제거할 수 있다.
### 결과
#### 성능 향상
- 대기열 위치 조회 성능 대폭 개선 (O(log N) 복잡도)
  - Redis의 Sorted Set을 활용하여 대기열 위치 조회를 O(log N) 복잡도로 개선했다
  - 기존의 관계형 데이터베이스 COUNT 쿼리 방식(O(N))에 비해, 100만 명 대기열에서도 약 20번의 연산으로 위치를 찾을 수 있어 성능이 크게 향상되었다.
- 실시간 대기열 상태 업데이트 가능
  - Redis의 인메모리 특성과 단일 스레드 모델을 활용, 실시간 상태 업데이트가 가능해졌다.
  - Redis의 원자적 명령어 실행을 통해 데이터 일관성을 유지하면서 밀리초 단위의 빠른 응답을 제공한다.
#### 확장성 증가
- 대규모 동시 접속 처리 가능
  - Redis의 초당 수만 건의 연산 처리 능력을 기반으로, 대규모 동시 접속 상황에서도 안정적으로 서비스를 제공한다.
  - Redis Cluster를 활용한 수평적 확장으로, 트래픽 증가에 유연하게 대응이 가능하다.
- 유연한 대기열 관리 (쉬운 확장 및 축소)
  - Redis Sorted Set을 이용해 대기열 크기를 동적으로 조절, 확장과 축소가 용이해졌다.
#### 실시간성 확보
- 대기열 상태 변경 즉시 반영
  - Redis의 인메모리 특성으로 디스크 I/O를 최소화, 대기열 상태 변경이 즉시 반영됨으로써 실시간 대기 시간 예측 및 제공이 가능해져 사용자 경험을 개선했다.
#### 🔗[Docs 7. Redis_Queue](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/07_Redis_Queue.md)

## ✔  캐시 도입을 통한 성능 개선
많은 요청이 발생할 것으로 예상되는 서비스 메소드에 캐시를 적용함으로써 데이터베이스 부하 감소와 응답시간을 단축이라는 목적을 달성하였다.
### 결과
#### 데이터베이스 부하 감소
- 반복적인 쿼리를 캐시로 대체하여 데이터베이스 부하를 크게 줄였다.
#### 응답 시간 단축
- 복잡한 쿼리 결과를 캐시에서 즉시 제공하여 응답 시간을 대폭 단축시킨다.
- 예: 콘서트별 스케쥴 조회 쿼리 시 **758 ms가 소요되던 요청이 캐시 히트 시 11ms로 크게 단축되었다.**
#### 데이터 일관성 유지
- 적절한 TTL 설정과 상태 변경 시 즉시 캐시 무효화를 통해 데이터의 일관성을 유지한다.

#### 🔗[Docs 8. Cache](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/08_Cache.md)

## ✔  쿼리 분석 및 인덱스 필요성 평가
잦은 조회가 일어날 것으로 예상되는 쿼리를 분석해 인덱싱 기능을 적용하여 조회 시간은 단축하고 데이터에 대한 접근 속도는 증가시켰다.
### 결과
#### 성능 개선
- 각각의 서비스들의 주요 쿼리에 대해 적절한 인덱스를 적용함으로써, 예상 쿼리 실행 시간을 85-95% 감소시킬 수 있을 것으로 기대한다.
- 특히 사용자 잔액 조회, 콘서트 스케줄 조회, 좌석 조회 등 빈번하게 사용되는 쿼리의 성능이 크게 향상될 것으로 예상된다.
#### 시스템 영향
- 인덱스 적용으로 인한 약간의 쓰기 성능 저하가 있을 수 있으나, 읽기 작업의 대폭적인 성능 향상으로 상쇄될 것으로 예상된다.
- 읽기 작업이 대부분의 쿼리 호출의 비중을 차지하므로 전체적인 시스템 응답 시간과 사용자 경험이 크게 개선될 것으로 기대된다.
#### 🔗[Docs 9. Index](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/09_Index.md)

## ✔ API 부하 테스트 분석과 장애 대응 방안
티켓팅 서비스의 특성상 특정 시간에 대량의 트래픽이 발생할 것이므로 이런 상황을 테스트 하기 위한 도구로 K6를 선택해 테스트를 진행했다.

K6를 통해 Load Test와 Peak Test를 진행하였으며 Prometheus와 Grafana를 통해 시각화하였고 테스트 도중 발생한 문제와 앞으로 발생할 문제를 해결하기 위한 방안을 고민하였다.

#### 콘서트 조회 및 예약 로직 Load Test 결과
![Image](https://github.com/user-attachments/assets/ee79ee06-36d1-4cff-8d1c-5216ec2f75ae)

### 개선 필요 영역
#### 응답 시간 일관성 향상
- 부하가 증가할수록 응답 시간이 급격히 증가하는 현상이 발생하고 있다.
- 트랜잭션 처리 방식 개선 및 비동기 처리, 큐 시스템 도입, 캐싱 최적화 등을 고려해야 한다.
#### 부하 분산
- 현재 단일 서버 환경에서 높은 부하가 발생하면서 특정 서버 인스턴스가 과부하 상태에 빠지는 현상이 확인된다.
- 이를 해결하기 위해 Load Balancing 및 Auto Scaling 전략을 도입하여 트래픽을 분산해야 한다.

### 개선 방법
#### 시도해본 방법
- **캐싱 적용**
  - 잔액 조회 API의 경우 실시간 데이터 변경 가능성 때문에 기존에는 캐싱을 적용하지 않았나 성능 개선을 위해 캐싱을 적용해보았다.
  - 성능 개선을 위해 캐싱을 적용했으나, 기대했던 응답 속도 개선이 나타나지 않고 오히려 증가하는 현상이 발생했다.
  - 예상 했던 결과는 아니었으나 현재 응답시간 증가의 원인이 DB 부하보다는 다른 요인이 성능 저하의 주요 원인이라는 것을 알 수 있었다.
#### 시도해볼 수 있는 방법
- **1. Auto Scaling 및 Load Balancing**
  - 현재 Docker 컨테이너 기반 배포 중이나, Auto Scaling이 적용되지 않아 트래픽 급증 시 부하를 감당하지 못하는 상태로 보인다.
  - 트래픽이 일정 임계치를 초과할 경우 서버 인스턴스를 자동으로 확장(Scale-out)하여 대응하는 구조가 필요하다.
  - Load Balancer를 적용하여 트래픽을 여러 서버로 균등하게 분배하는 방식도 함께 적용해야 한다.
- **2. 클라우드 기반 배포 (Cloud Deployment) 전환**
  - 현재 로컬 환경에서 배포 중이며, CPU 및 RAM 사용량이 급증하는 것이 성능 저하의 주요 원인으로 추정된다.
  - AWS, GCP, Azure 등의 클라우드 환경으로 전환하여, 수직적 확장(Scale-up) 및 수평적 확장(Scale-out) 전략을 동시에 적용하는 것이 필요하다.
- **3. 비동기 처리 및 메시지 큐 도입**
  - 현재 API 요청이 동기(Synchronous) 방식으로 처리되며, 일부 트랜잭션에서 병목이 발생하는 것으로 보인다.
  - Kafka, RabbitMQ 등의 메시지 큐(Message Queue) 시스템을 활용하여 비동기 이벤트 처리 구조를 도입할 필요가 있다.
- **4. DB 최적화 및 Connection Pooling 개선**
  - 현재 DB 부하가 성능 저하의 주요 원인이 아닐 가능성이 높으나, 쿼리 최적화 및 Connection Pool 설정을 조정할 필요가 있다.
  - Read/Write 분리, 인덱스 최적화, 캐싱 레이어(Redis, Memcached) 활용 등을 고려해야 한다.
### 결론
- 현재 시스템은 전반적으로 안정적인 성능을 보이며, 최대 2000명의 동시 접속을 처리할 수 있다.
- 그러나 높은 부하 상황에서 응답 시간이 급격히 증가하는 문제가 있고, 이는 로컬 환경의 한계로 보이며, 이로 인해 사용자 경험이 저하될 가능성이 있다.
- 이를 해결하기 위해 Auto Scaling, Load Balancing, 클라우드 배포, 메시지 큐 도입을 통한 비동기 통신 구현 등의 방안을 적극적으로 고려해야 한다.
#### 🔗[Docs 10. Incident_Response](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/10_Incident_Response.md)

<br>

---
# 🔧 기술 스택

`Backend`

- intelliJ IDE
- springboot 3.3.5
- spring cloud netflix eureka
- spring cloud gateway
- spring cloud zipkin
- spring cloud openfeign
- spring Data JPA
- java 21
- mockito
- jwt
- junit
- gradle
- swagger

`Data`

- mySQL
- redis
- kafka

`Infra`

- GKE
- kubernetes
- nginx
- jenkins
- docker
- docker-compose

`test`

- k6

`monitoring`
- prometheus
- grafana

`etc`
- github
- notion

<br>

---
# 📃 프로젝트 산출물
- 🔗[마일스톤](https://github.com/JonghyunJoo/Spring_Cloud_ConcertReservation/blob/master/docs/01_Milestone.md)

- 🔗[이벤트 시퀀스 다이어그램](https://github.com/JonghyunJoo/Spring_Cloud_ConcertReservation/blob/master/docs/02_EventSequence.md)

- 🔗[ERD](https://flossy-name-c7c.notion.site/ERD-1a38f15d8fbc800aa5e1cb768e0e0d1b))

- 🔗[API 명세 문서](https://flossy-name-c7c.notion.site/Spring-Cloud-ConcertReservation-1908f15d8fbc80ddb4ddcd3284892151)

- 🔗[Git Commit 전략](https://flossy-name-c7c.notion.site/Git-Commit-1058f15d8fbc8094922ecbeefa3ef78d)
