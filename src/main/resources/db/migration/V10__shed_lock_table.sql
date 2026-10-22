CREATE TABLE shed_lock(
          id BIGINT AUTO_INCREMENT PRIMARY KEY,
          name VARCHAR(64) UNIQUE NOT NULL,
          lock_until TIMESTAMP(3) NOT NULL,
          locked_at TIMESTAMP(3) NOT NULL,
          locked_by VARCHAR(255) NOT NULL
);