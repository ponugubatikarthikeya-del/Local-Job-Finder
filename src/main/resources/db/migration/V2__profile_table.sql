CREATE TABLE user_profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  headline VARCHAR(100),
  skills JSON,
  date_of_birth DATE,
  city_name VARCHAR(100),
  experience_level VARCHAR(50),
  rating DECIMAL(5,2) DEFAULT 0,
  total_jobs_completed INT DEFAULT 0,
  user_description VARCHAR(100),
  latitude DECIMAL(10,8),
  longitude DECIMAL(11,8),
  availability_status ENUM('AVAILABLE','BUSY') DEFAULT 'AVAILABLE',
  CONSTRAINT fk_worker_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_worker_location ON user_profiles(latitude, longitude);
