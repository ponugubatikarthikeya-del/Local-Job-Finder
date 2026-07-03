CREATE TABLE IF NOT EXISTS worker_profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  skills JSON,
  experience_level VARCHAR(50),
  rating DECIMAL(5,2) DEFAULT 0,
  total_jobs_completed INT DEFAULT 0,
  latitude DECIMAL(10,8),
  longitude DECIMAL(11,8),
  availability_status ENUM('AVAILABLE','BUSY') DEFAULT 'AVAILABLE',
  CONSTRAINT fk_worker_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_worker_location ON worker_profiles(latitude, longitude);
