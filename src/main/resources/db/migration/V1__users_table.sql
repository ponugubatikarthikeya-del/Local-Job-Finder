CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL,
                       phone VARCHAR(20),
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) DEFAULT NULL, -- NULLABLE for social signups (Google, GitHub, Facebook)
                       auth_provider ENUM('LOCAL', 'HYBRID', 'GOOGLE', 'GITHUB', 'FACEBOOK') DEFAULT 'LOCAL',
--                        google_id VARCHAR(255) DEFAULT NULL UNIQUE,
--                        github_id VARCHAR(255) DEFAULT NULL UNIQUE,
--                        facebook_id VARCHAR(255) DEFAULT NULL UNIQUE,
                       account_status ENUM('VERIFIED', 'UNVERIFIED') DEFAULT 'UNVERIFIED',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
