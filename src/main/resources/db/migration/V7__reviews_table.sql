CREATE TABLE reviews (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         job_id BIGINT NOT NULL,
                         reviewer_id BIGINT NOT NULL,
                         receiver_id BIGINT NOT NULL,
                         rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         comment TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_review_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
                         CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT fk_review_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    -- Rule: A user cannot review the same job twice
                         CONSTRAINT unique_job_reviewer UNIQUE (job_id, reviewer_id)
);

CREATE INDEX idx_reviews_receiver ON reviews(receiver_id);