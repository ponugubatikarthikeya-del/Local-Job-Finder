package com.localjobfinder.bakend.dbEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
@Table(name = "tokens",uniqueConstraints = {
        @UniqueConstraint(name = "unique_user_device", columnNames = {"user_id", "device_id"})
}, indexes = {
        @Index(name = "idx_users_otp_hash", columnList = "otp_hash"),
        @Index(name = "idx_tokens_user", columnList = "user_id"),
        @Index(name = "idx_users_hash_reset_key",columnList = "hash_reset_key")

})
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "device_id", nullable = false)
    private String deviceId; // e.g. Android's Settings.Secure.ANDROID_ID or custom UUID generated on client

    @Column(name = "hash_refresh_token")
    private String hashRefreshToken;

    @Column(name = "hash_access_token")
    private String hashAccessToken;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "hash_reset_key")
    private String hashResetKey;

    @Column(name = "otp_hash")
    private String otpHash;

    @Column(name = "otp_expiry")
    private Instant otpExpiry;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
