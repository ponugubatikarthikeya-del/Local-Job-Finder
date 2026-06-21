CREATE TABLE job_applications (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  job_id BIGINT NOT NULL,
                                  worker_id BIGINT NOT NULL,
                                  status ENUM('APPLIED', 'ACCEPTED', 'REJECTED', 'COMPLETED') DEFAULT 'APPLIED',
                                  applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_app_worker FOREIGN KEY (worker_id) REFERENCES users(id) ON DELETE CASCADE,

                                  UNIQUE KEY unique_application (job_id, worker_id)
);

CREATE INDEX idx_app_status ON job_applications(status);