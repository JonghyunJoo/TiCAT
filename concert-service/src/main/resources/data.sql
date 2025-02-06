-- DROP TABLE IF EXISTS flights;
CREATE TABLE IF NOT EXISTS concerts (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          title VARCHAR(255) NOT NULL,       -- 콘서트 제목
                          stage VARCHAR(255) NOT NULL,       -- 공연 장소
                          concert_start_date DATETIME NOT NULL, -- 콘서트 시작일
                          concert_end_date DATETIME NOT NULL,   -- 콘서트 종료일
                          created_at DATETIME NOT NULL, -- 생성 시간
                          updated_at DATETIME NOT NULL
--                           updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 수정 시간
);

CREATE TABLE IF NOT EXISTS concert_Schedules (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  concert_id BIGINT NOT NULL,        -- 연결된 콘서트 ID (FK)
                                  date DATE NOT NULL,                -- 공연 날짜
                                  start_time TIME NOT NULL,          -- 공연 시작 시간
                                  total_seats BIGINT NOT NULL,          -- 총 좌석 수
                                  seat_pricing TEXT,                 -- 좌석 가격 정보 (JSON 등으로 저장 가능)
                                  CONSTRAINT fk_concert FOREIGN KEY (concert_id) REFERENCES Concerts (id) ON DELETE CASCADE
);

INSERT INTO concerts (title, stage, concert_start_date, concert_end_date, created_at, updated_at)
VALUES
    ('2025 BTS World Tour', '서울 올림픽공원 KSPO DOME', '2025-03-01 18:00:00', '2025-03-01 21:00:00', NOW(), NOW()),
    ('IU 2025 Concert', '부산 벡스코', '2025-04-15 19:00:00', '2025-04-15 22:00:00', NOW(), NOW()),
    ('BLACKPINK Encore', '인천 아시아드 주경기장', '2025-05-20 17:00:00', '2025-05-20 20:00:00', NOW(), NOW());

INSERT INTO concert_Schedules (concert_id, date, start_time, total_seats, seat_pricing)
VALUES
    -- BTS World Tour
    (1, '2025-03-01', '18:00:00', 8, '{"VIP": 220000, "R석": 180000, "S석": 150000, "A석": 100000}'),
    (1, '2025-03-02', '18:00:00', 8, '{"VIP": 220000, "R석": 180000, "S석": 150000, "A석": 100000}'),

    -- IU 2025 Concert
    (2, '2025-04-15', '19:00:00', 6, '{"VIP": 200000, "R석": 160000, "S석": 120000}'),
    (2, '2025-04-16', '19:00:00', 6, '{"VIP": 200000, "R석": 160000, "S석": 120000}'),

    -- BLACKPINK Encore
    (3, '2025-05-20', '17:00:00', 8, '{"VIP": 250000, "R석": 200000, "S석": 170000, "A석": 130000}'),
    (3, '2025-05-21', '17:00:00', 8, '{"VIP": 250000, "R석": 200000, "S석": 170000, "A석": 130000}');