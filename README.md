# 🎫 대규모 트래픽에도 안정적인 콘서트 티켓팅 플랫폼


- [프로젝트 개요](#✨-프로젝트-개요)

- [목표 & KPI](#🎯-목표-&-KPI(핵심성과지표))

- [주요 기술](#‍💻-주요-기술)
  - [MSA](#✔-MSA)
    - msa 설계도
    - msa를 도입하며 얻은 이점
    - Spring Cloud Gateway 및 Spring Eureka
  - [배포 및 CI/CD (25.02~)](#✔-배포-및-CI/CD-(25.02-~))
    - 아키텍처 설계도
    - 배포 과정
    - 배포 환경의 변화로 얻은 이점
  - [동시성 문제와 극복](#✔-동시성-문제와-극복)
  - [대기열 시스템 설계 및 Redis 이관](#✔-대기열-시스템-설계-및-Redis-이관)
  - [캐시 도입을 통한 성능 개선](#✔-캐시-도입을-통한-성능-개선)
  - [쿼리 분석 및 인덱스 필요성 평가](#✔-쿼리-분석-및-인덱스-필요성-평가)
  - [API 부하 테스트 분석과 장애 대응 방안](#✔-API-부하-테스트-분석과-장애-대응-방안)

- [기술 스택](#🔧-기술-스택)
- [프로젝트 산출물](#📃-프로젝트-산출물)
- [Commit & Branch 전략](💑Commit-&-Branch-전략)
<br>

---

# ✨ 서비스 개요

**특정한 시각에 사람들이 한꺼번에 몰려 트래픽이 집중되는 것을 가정한 티케팅 사이트**

**이러한 상황에서도 서버가 터지지 않게 하기 위한 안정적인 아키텍처를 고민해보고 프로젝트에 적용**

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

## 2. 공정한 예약 시스템 제공
- 중복 예약 방지
  - 동시성 제어를 통한 **중복 예약 발생율 0% 목표**


- 예매 실패자를 위한 취소표 알림 시스템 운영
  - 대기열 혹은 동시성 제어로 인해 예매하지 못한 유저 대상으로 **취소표 알림 기능 제공**

## 3. 모니터링 및 장애 대응 체계 구축

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
대규모 트래픽이 발생하는 상황에서 생길 수 밖에 없는 동시성 이슈에 대해 인지하고 이를 처리하기 위해 DB 락, 분산 락 등의 방안을 적용하고 비교해 문제를 극복해보았다.

#### 🔗[Docs 6. ConcurrencyReport](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/06_ConcurrencyReport.md)
## ✔ 대기열 시스템 설계 및 Redis 이관
많은 수의 이용자가 한꺼번에 요청을 시도할 것으로 예상되는 서비스들에 대해 DB의 처리량을 고려하여 순서를 보장하는 대기열 시스템을 통해 안정적인 요청 처리를 구현하였다.

#### 🔗[Docs 7. Redis_Queue](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/07_Redis_Queue.md)
## ✔  캐시 도입을 통한 성능 개선
많은 요청이 발생할 것으로 예상되는 서비스 메소드에 캐시를 적용함으로써 데이터베이스 부하 감소와 응답시간을 단축하였다.

#### 🔗[Docs 8. Cache](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/08_Cache.md)

## ✔  쿼리 분석 및 인덱스 필요성 평가
잦은 조회가 일어날 것으로 예상되는 쿼리를 분석해 인덱싱 기능을 적용하여 조회 속도는 단축하고 데이터에 대한 접근 속도는 증가시켰다.

#### 🔗[Docs 9. Index](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/09_Index.md)
## ✔ API 부하 테스트 분석과 장애 대응 방안
티켓팅 서비스의 특성상 특정 시간에 대량의 트래픽이 발생할 것이므로 이런 상황을 테스트 하기 위한 도구로 K6를 선택해 테스트를 진행했다.

K6를 통해 Load Test와 Peak Test를 진행하였으며 Prometheus와 Grafana를 통해 시각화하였고 테스트 도중 발생한 문제와 앞으로 발생할 문제를 해결하기 위한 방안을 고민하였다. 

#### 🔗[Docs 10. Incident_Response](https://github.com/JonghyunJoo/ConcertReservation/blob/master/docs/10_Incident_Response.md)

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

- 🔗[ERD](https://github.com/JonghyunJoo/Spring_Cloud_ConcertReservation/blob/master/docs/03_ERD.md)

- 🔗[API 명세 문서](https://flossy-name-c7c.notion.site/Spring-Cloud-ConcertReservation-1908f15d8fbc80ddb4ddcd3284892151)

- 🔗[Git Commit 전략](#https://flossy-name-c7c.notion.site/Git-Commit-1058f15d8fbc8094922ecbeefa3ef78d)
