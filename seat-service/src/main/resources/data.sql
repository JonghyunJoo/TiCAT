CREATE TABLE IF NOT EXISTS seat (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    flight_id BIGINT NOT NULL,
                                    price BIGINT NOT NULL,
                                    seat_grade VARCHAR(255) NOT NULL,
    seat_status VARCHAR(255) NOT NULL,
    number INT NOT NULL,
    FOREIGN KEY (flight_id) REFERENCES flight(id)
    );

insert into seat (flight_id, price, seat_grade, seat_status, number)
values
    (1, 500000, 'ECONOMY', 'AVAILABLE', 1),
    (1, 700000, 'ECONOMY', 'LOCKED', 2),
    (1, 1000000, 'BUSINESS', 'RESERVED', 3),
    (1, 1200000, 'BUSINESS', 'AVAILABLE', 4),
    (1, 1500000, 'FIRST', 'AVAILABLE', 5),
    (1, 2000000, 'FIRST', 'RESERVED', 6);
