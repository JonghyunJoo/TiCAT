
```mermaid
erDiagram
    User {
        bigint id PK
        string email UNIQUE
        string name
        string encryptedPwd
    }

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
        bigint concertId FK
        date date
        time startTime
        bigint totalSeats
        text seatPricing
    }

    Seat {
        bigint id PK
        bigint concertScheduleId FK
        bigint userId FK
        bigint price
        enum seatGrade
        enum seatStatus
        int rowNumber
        int columnNumber
    }

    ReservationGroup {
        bigint id PK
        bigint userId FK
        enum status
        datetime createdAt
        datetime updatedAt
    }

    Reservation {
        bigint id PK
        bigint userId FK
        bigint seatId FK
        enum reservationStatus
        bigint price
        datetime createdAt
        datetime updatedAt
        bigint reservationGroupId FK
    }

    %% Queue Service
    Queue {
        bigint userId PK
        bigint concertScheduleId FK
        bigint requestTime
    }

    Payment {
        bigint id PK
        bigint userId FK
        bigint amount
        enum status
        datetime createdAt
    }

    Wallet {
        bigint id PK
        bigint userId FK
        bigint balance
    }

    TransactionHistory {
        bigint id PK
        bigint userId FK
        bigint amount
        string type
        datetime createdAt
        enum transactionType
        bigint balanceAfterTransaction
    }

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

