
create table tokens(
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   user_id BIGINT NOT NULL,
   device_id varchar(255) NOT NULL,
   hash_refresh_token varchar(512),
   hash_access_token varchar(512),
   token_expires_at TIMESTAMP,
   hash_reset_key varchar(512),
   otp_hash VARCHAR(512),
   otp_expiry DATETIME,
   revoked BOOLEAN DEFAULT FALSE,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT unique_user_device UNIQUE (user_id,device_id)
);

CREATE INDEX idx_tokens_user ON tokens(user_id);
CREATE INDEX idx_users_otp_hash ON tokens(otp_hash);
CREATE INDEX idx_users_hash_reset_key ON tokens(hash_reset_key);


