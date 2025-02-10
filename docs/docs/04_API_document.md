# 콘서트 예약 시스템 API 문서

## 목차
1. [사용자 회원 가입](#1-사용자-회원-가입)
2. [전체 사용자 목록 조회](#2-전체-사용자-목록-조회)
3. [사용자 정보 상세 조회](#3-사용자-정보-상세-조회)
4. [콘서트 생성](#4-콘서트-생성)
5. [콘서트 검색](#5-콘서트-검색)
6. [콘서트 정보 조회](#6-콘서트-정보-조회)
7. [콘서트 스케줄 생성](#7-콘서트-스케줄-생성)
8. [콘서트 스케줄 조회](#8-콘서트-스케줄-조회)
9. [콘서트 스케줄 단건 조회](#9-콘서트-스케줄-단건-조회)
10. [콘서트 삭제](#10-콘서트-삭제)
11. [콘서트 스케줄 삭제](#11-콘서트-스케줄-삭제)

---

## 1. 사용자 회원 가입

### Description
사용자가 회원 가입을 요청하는 API입니다.

### Request

- **URL**: `user-service/users`
- **Method**: `POST`
- **Request Body**:

```json
{
  "email": "user@example.com",
  "name": "홍길동",
  "balance": 10000
}
```

### Response

```json
{
  "email": "user@example.com",
  "name": "홍길동",
  "balance": 10000
}
```

### Error

```json
{
  "status": 400,
  "error": "BAD REQUEST",
  "message": "유효하지 않은 요청입니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 2. 전체 사용자 목록 조회

### Description
현재 가입된 전체 사용자 목록을 조회하는 API입니다.

### Request

- **URL**: `user-service/users`
- **Method**: `GET`

### Response

```json
[
  {
    "email": "user1@example.com",
    "name": "김철수",
    "balance": 5000
  },
  {
    "email": "user2@example.com",
    "name": "이영희",
    "balance": 20000
  }
]
```

### Error

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 3. 사용자 정보 상세 조회

### Description
특정 사용자의 정보를 조회하는 API입니다.

### Request

- **URL**: `user-service/users/{Id}`
- **Method**: `GET`
- **URL Params**:  
  - `Id` (Long) - 사용자 ID

### Response

```json
{
  "email": "user@example.com",
  "name": "홍길동",
  "balance": 10000
}
```

### Error

```json
{
  "status": 404,
  "error": "NOT FOUND",
  "message": "사용자를 찾을 수 없습니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 4. 콘서트 생성

### Description
새로운 콘서트를 생성하는 API입니다.

### Request

- **URL**: `/`
- **Method**: `POST`
- **Request Body**:

```json
{
  "title": "콘서트 A",
  "stage": "서울 올림픽 체조 경기장",
  "concertStartDate": "2025-01-15T08:00:00",
  "concertEndDate": "2025-01-15T11:00:00"
}
```

### Response

- **Status Code**: 201 Created
- **Response Body**:

```json
{
  "id": 1,
  "title": "콘서트 A",
  "stage": "서울 올림픽 체조 경기장",
  "concertStartDate": "2025-01-15T08:00:00",
  "concertEndDate": "2025-01-15T11:00:00"
}
```

### Error

```json
{
  "status": 400,
  "error": "BAD REQUEST",
  "message": "잘못된 요청 데이터입니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 5. 콘서트 검색

### Description
입력 조건을 기반으로 콘서트를 검색하는 API입니다.

### Request

- **URL**: `/`
- **Method**: `GET`
- **Request Body**:

```json
{
  "title": "콘서트 A",
  "stage": "서울 올림픽 체조 경기장",
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-12-31T23:59:59"
}
```

### Response

```json
{
  "content": [
    {
      "id": 1,
      "title": "콘서트 A",
      "stage": "서울 올림픽 체조 경기장",
      "concertStartDate": "2025-01-15T08:00:00",
      "concertEndDate": "2025-01-15T11:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": false,
      "unsorted": true,
      "empty": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": false,
    "unsorted": true,
    "empty": true
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

### Error

```json
{
  "status": 404,
  "error": "NOT FOUND",
  "message": "조건에 맞는 콘서트가 없습니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 6. 콘서트 정보 조회

### Description
ID를 기준으로 콘서트 정보를 조회하는 API입니다.

### Request

- **URL**: `/{concertId}`
- **Method**: `GET`
- **URL Params**:  
  - `concertId` (Long) - 콘서트 ID

### Response

```json
{
  "id": 1,
  "title": "콘서트 A",
  "stage": "서울 올림픽 체조 경기장",
  "concertStartDate": "2025-01-15T08:00:00",
  "concertEndDate": "2025-01-15T11:00:00"
}
```

### Error

```json
{
  "status": 404,
  "error": "NOT FOUND",
  "message": "해당 ID의 콘서트가 없습니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 7. 콘서트 스케줄 생성

### Description
콘서트의 스케줄을 생성하는 API입니다.

### Request

- **URL**: `/concertSchedule`
- **Method**: `POST`
- **Request Body**:

```json
{
  "concertId": 1,
  "date": "2025-01-30",
  "startTime": "19:30:00",
  "totalSeats": 300,
  "availableSeats": 300,
  "seatPricing": "{\"VIP\": 150, \"Regular\": 100}"
}
```

### Response

- **Status Code**: 201 Created
- **Response Body**:

```json
{
  "id": 1001,
  "concertId": 1,
  "date": "2025-01-30",
  "startTime": "19:30:00",
  "totalSeats": 300,
  "availableSeats": 300,
  "seatPricing": "{\"VIP\": 150, \"Regular\": 100}"
}
```

### Error

```json
{
  "status": 400,
  "error": "BAD REQUEST",
  "message": "잘못된 요청 데이터입니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 8. 콘서트 스케줄 조회

### Description
콘서트 ID를 기준으로 모든 스케줄을 조회하는 API입니다.

### Request

- **URL**: `/concertSchedule/{concertId}`
- **Method**: `GET`
- **URL Params**:  
  - `concertId` (Long) - 콘서트 ID

### Response

```json
[
  {
    "id": 1001,
    "concertId": 1,
    "date": "2025-01-30",
    "startTime": "19:30:00",
    "totalSeats": 300,
    "availableSeats": 300,
    "seatPricing": "{\"VIP\": 150, \"Regular\": 100}"
  }
]
```

### Error

```json
{
  "status": 404,
  "error": "NOT FOUND",
  "message": "해당 콘서트의 스케줄이 없습니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 9. 콘서트 스케줄 단건 조회

### Description
`concertScheduleId`를 기준으로 특정 콘서트 스케줄을 조회하는 API입니다.

### Request

- **URL**: `/concertSchedule/detail/{concertScheduleId}`
- **Method**: `GET`
- **URL Params**:  
  - `concertScheduleId` (Long) - 콘서트 스케줄 ID

### Response

```json
{
  "id": 1001,
  "concertId": 1,
  "date": "2025-01-30",
  "startTime": "19:30:00",
  "totalSeats": 300,
  "availableSeats": 300,
  "seatPricing": "{\"VIP\": 150, \"Regular\": 100}"
}
```

### Error

```json
{
  "status": 404,
  "error": "NOT FOUND",
  "message": "해당 ID의 스케줄이 없습니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 10. 콘서트 삭제

### Description
`concertId`를 기준으로 콘서트를 삭제하는 API입니다.

### Request

- **URL**: `/{concertId}`
- **Method**: `DELETE`
- **URL Params**:  
  - `concertId` (Long) - 콘서트 ID

### Response

- **Status Code**: 204 No Content

### Error

```json
{
  "status": 404,
  "error": "NOT FOUND",
  "message": "해당 ID의 콘서트가 없습니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```

---

## 11. 콘서트 스케줄 삭제

### Description
`concertScheduleId`를 기준으로 콘서트 스케줄을 삭제하는 API입니다.

### Request

- **URL**: `/concertSchedules/{concertScheduleId}`
- **Method**: `DELETE`
- **URL Params**:  
  - `concertScheduleId` (Long) - 콘서트 스케줄 ID

### Response

- **Status Code**: 204 No Content

### Error

```json
{
  "status": 404,
  "error": "NOT FOUND",
  "message": "해당 ID의 스케줄이 없습니다."
}
```

```json
{
  "status": 500,
  "error": "INTERNAL SERVER ERROR",
  "message": "서버 오류가 발생했습니다."
}
```