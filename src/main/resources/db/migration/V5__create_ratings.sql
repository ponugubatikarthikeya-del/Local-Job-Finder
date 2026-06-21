CREATE TABLE ratings (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         job_id BIGINT NOT NULL,
                         from_user_id BIGINT NOT NULL,
                         to_user_id BIGINT NOT NULL,
                         rating INT CHECK (rating BETWEEN 1 AND 5),
                         review TEXT,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_rating_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
                         CONSTRAINT fk_rating_from FOREIGN KEY (from_user_id) REFERENCES users(id),
                         CONSTRAINT fk_rating_to FOREIGN KEY (to_user_id) REFERENCES users(id)
);