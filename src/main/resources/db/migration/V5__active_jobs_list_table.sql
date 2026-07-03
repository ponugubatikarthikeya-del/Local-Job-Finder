CREATE TABLE active_listings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    location POINT NOT NULL,
    pay_amount DECIMAL(10, 2),
    category VARCHAR(50),
    expiry_at DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    SPATIAL INDEX idx_location (location),
    CONSTRAINT fk_active_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_active_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);