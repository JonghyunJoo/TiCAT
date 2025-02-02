CREATE TABLE IF NOT EXISTS seats (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        concert_schedule_id BIGINT NOT NULL, -- 연결된 콘서트 스케줄 ID
                        user_id BIGINT,                      -- 예약자 ID (NULL 가능)
                        price BIGINT NOT NULL,               -- 좌석 가격
                        seat_grade VARCHAR(255) NOT NULL,    -- 좌석 등급
                        seat_status VARCHAR(255) NOT NULL,
                        row_number INT NOT NULL,             -- 좌석의 행 번호
                        column_number  INT NOT NULL          -- 좌석의 열 번호
);

-- BTS World Tour (Schedule 1: 2025-03-01)
INSERT INTO seats (concert_schedule_id, user_id, price, seat_grade, seat_status, row_number, column_number)
VALUES
    (1, NULL, 220000, 'VIP', 'AVAILABLE', 1, 1),
    (1, NULL, 220000, 'VIP', 'LOCKED', 1, 2),
    (1, NULL, 180000, 'R', 'AVAILABLE', 2, 1),
    (1, NULL, 180000, 'R', 'RESERVED', 2, 2),
    (1, NULL, 150000, 'S', 'AVAILABLE', 3, 1),
    (1, NULL, 150000, 'S', 'AVAILABLE', 3, 2),
    (1, NULL, 100000, 'A', 'AVAILABLE', 4, 1),
    (1, NULL, 100000, 'A', 'AVAILABLE', 4, 2);

-- BTS World Tour (Schedule 2: 2025-03-02)
INSERT INTO seats (concert_schedule_id, user_id, price, seat_grade, seat_status, row_number, column_number)
VALUES
    (2, NULL, 220000, 'VIP', 'AVAILABLE', 1, 1),
    (2, NULL, 220000, 'VIP', 'AVAILABLE', 1, 2),
    (2, NULL, 180000, 'R', 'AVAILABLE', 2, 1),
    (2, NULL, 180000, 'R', 'AVAILABLE', 2, 2),
    (2, NULL, 150000, 'S', 'AVAILABLE', 3, 1),
    (2, NULL, 150000, 'S', 'AVAILABLE', 3, 2),
    (2, NULL, 100000, 'A', 'AVAILABLE', 4, 1),
    (2, NULL, 100000, 'A', 'AVAILABLE', 4, 2);

-- IU 2025 Concert (Schedule 3: 2025-04-15)
INSERT INTO seats (concert_schedule_id, user_id, price, seat_grade, seat_status, row_number, column_number)
VALUES
    (3, NULL, 200000, 'VIP', 'AVAILABLE', 1, 1),
    (3, NULL, 200000, 'VIP', 'AVAILABLE', 1, 2),
    (3, NULL, 160000, 'R', 'AVAILABLE', 2, 1),
    (3, NULL, 160000, 'R', 'AVAILABLE', 2, 2),
    (3, NULL, 120000, 'S', 'AVAILABLE', 3, 1),
    (3, NULL, 120000, 'S', 'AVAILABLE', 3, 2);

-- BLACKPINK Encore (Schedule 4: 2025-05-20)
INSERT INTO seats (concert_schedule_id, user_id, price, seat_grade, seat_status, row_number, column_number)
VALUES
    (5, NULL, 250000, 'VIP', 'AVAILABLE', 1, 1),
    (5, NULL, 250000, 'VIP', 'AVAILABLE', 1, 2),
    (5, NULL, 200000, 'R', 'AVAILABLE', 2, 1),
    (5, NULL, 200000, 'R', 'AVAILABLE', 2, 2),
    (5, NULL, 170000, 'S', 'AVAILABLE', 3, 1),
    (5, NULL, 170000, 'S', 'AVAILABLE', 3, 2),
    (5, NULL, 130000, 'A', 'AVAILABLE', 4, 1),
    (5, NULL, 130000, 'A', 'AVAILABLE', 4, 2);
