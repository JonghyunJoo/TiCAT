DROP TABLE IF EXISTS flights;
CREATE TABLE flights (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         departure VARCHAR(20) NOT NULL,
                         destination VARCHAR(20) NOT NULL,
                         departure_time DATETIME NOT NULL,
                         arrival_time DATETIME NOT NULL
);

INSERT INTO flights (departure, destination, departure_time, arrival_time) VALUES
('ICN', 'JFK', '2025-01-10 14:00:00', '2025-01-10 18:00:00'),
('JFK', 'LHR', '2025-01-11 09:30:00', '2025-01-11 14:00:00'),
('LHR', 'FRA', '2025-01-12 16:45:00', '2025-01-12 22:00:00'),
('FRA', 'HND', '2025-01-13 07:15:00', '2025-01-13 10:30:00'),
('HND', 'ICN', '2025-01-14 20:00:00', '2025-01-15 04:15:00');
