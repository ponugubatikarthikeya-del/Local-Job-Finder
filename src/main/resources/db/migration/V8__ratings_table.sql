CREATE TABLE user_ratings_summary (
      user_id BIGINT PRIMARY KEY,
      total_stars BIGINT DEFAULT 0,
      review_count BIGINT DEFAULT 0,
      average_rating DECIMAL(3,2) DEFAULT 0.00,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

      CONSTRAINT fk_summary_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for instant profile query lookups
CREATE INDEX idx_summary_user_id ON user_ratings_summary(user_id);