
```mermaid
erDiagram
    %% User Service
    User {
        bigint id PK
        string email UNIQUE
        string name
        string encryptedPwd
    }

    %% Concert Service
    Concert {
        bigint id PK
        string title
        string stage
        datetime concertStartDate
        datetime concertEndDate
        datetime createdAt
        datetime updatedAt
    }

    ConcertSchedule {
        bigint id PK
        bigint concertId FK (API 조회)
        date date
        time startTime
        bigint totalSeats
        text seatPricing
    }

    %% Seat Service
    Seat {
        bigint id PK
        bigint concertScheduleId FK (API 조회)
        bigint userId FK (API 조회)
        bigint price
        enum seatGrade
        enum seatStatus
        int rowNumber
        int columnNumber
    }

    %% Reservation Service
    ReservationGroup {
        bigint id PK
        bigint userId FK (API 조회)
        enum status
        datetime createdAt
        datetime updatedAt
    }

    Reservation {
        bigint id PK
        bigint userId FK (API 조회)
        bigint seatId FK (API 조회)
        enum reservationStatus
        bigint price
        datetime createdAt
        datetime updatedAt
        bigint reservationGroupId FK
    }

    %% Queue Service
    Queue {
        bigint userId PK (API 조회)
        bigint concertScheduleId FK (API 조회)
        bigint requestTime
    }

    %% Payment Service
    Payment {
        bigint id PK
        bigint userId FK (API 조회)
        bigint amount
        enum status
        datetime createdAt
    }

    %% Wallet Service
    Wallet {
        bigint id PK
        bigint userId FK (API 조회)
        bigint balance
    }

    TransactionHistory {
        bigint id PK
        bigint userId FK (API 조회)
        bigint amount
        string type
        datetime createdAt
        enum transactionType
        bigint balanceAfterTransaction
    }

    %% MSA 관계 (API 통신 기반)
    User ||..|| Wallet : owns
    User ||..|| ReservationGroup : makes
    User ||..|| Queue : joins
    User ||..|| Payment : makes
    User ||..|| TransactionHistory : records
    Concert ||--|| ConcertSchedule : has
    ConcertSchedule ||--|| Seat : has
    Seat ||..|| Reservation : assigned_to
    ReservationGroup ||--|| Reservation : contains
    Wallet ||--|| TransactionHistory : has

```

