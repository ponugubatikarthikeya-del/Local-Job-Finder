CREATE TABLE user_fcm_tokens (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL, -- Changed from VARCHAR to BIGINT
                                 device_token VARCHAR(255) NOT NULL UNIQUE,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Added missing comma
                                 CONSTRAINT fk_fcm_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
