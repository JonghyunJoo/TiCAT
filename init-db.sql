CREATE DATABASE IF NOT EXISTS user_service;
CREATE DATABASE IF NOT EXISTS concert_service;
CREATE DATABASE IF NOT EXISTS seat_service;
CREATE DATABASE IF NOT EXISTS reservation_service;
CREATE DATABASE IF NOT EXISTS payment_service;
CREATE DATABASE IF NOT EXISTS wallet_service;

GRANT ALL PRIVILEGES ON user_service.* TO 'msa_user'@'%';
GRANT ALL PRIVILEGES ON concert_service.* TO 'msa_user'@'%';
GRANT ALL PRIVILEGES ON seat_service.* TO 'msa_user'@'%';
GRANT ALL PRIVILEGES ON reservation_service.* TO 'msa_user'@'%';
GRANT ALL PRIVILEGES ON payment_service.* TO 'msa_user'@'%';
GRANT ALL PRIVILEGES ON wallet_service.* TO 'msa_user'@'%';

FLUSH PRIVILEGES;
