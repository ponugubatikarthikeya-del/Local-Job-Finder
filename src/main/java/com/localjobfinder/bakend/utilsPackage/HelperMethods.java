package com.localjobfinder.bakend.utilsPackage;

import com.localjobfinder.bakend.OauthPackage.AuthLoginEnum;
import com.localjobfinder.bakend.dbEntity.TokenEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.TokenRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class HelperMethods {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Autowired
    public HelperMethods(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    // Helper method to keep provider-linking logic modular and clean
    public void linkProviderId(UserEntity user, AuthLoginEnum provider, String providerId) {
        if (providerId == null || providerId.isBlank()) return;

        switch (provider) {
            case GOOGLE -> {
                if (user.getGoogleId() == null) user.setGoogleId(providerId);
            }
            case GITHUB -> {
                if (user.getGithubId() == null) user.setGithubId(providerId);
            }
            case FACEBOOK -> {
                if (user.getFacebookId() == null) user.setFacebookId(providerId);
            }
            default -> { /* LOCAL or HYBRID don't have provider IDs */}
        }
    }

    /**
     * Multi-Device Lookup: Manages user sessions by enforcing a device threshold limit.
     * If the user logs in from a new device and exceeds the maximum limit, the oldest
     * session row is securely recycled and cleared of past token fingerprints.
     */
    public TokenEntity getOrCreateTokenEntityForUser(UserEntity user, String deviceId) {

        // 1. Structural Fail-Fast Validations
        if (user == null || user.getId() == null) {
            throw new CustomException(ErrorMsg.AUTH_004, 404);
        }

        if (deviceId == null || deviceId.isBlank()) {
            throw new CustomException(ErrorMsg.DEVICE_001, 400);
        }

        // 2. Exact Match Check: If this exact device already has a row, reuse it.
        Optional<TokenEntity> existingToken = tokenRepository.findByUserIdAndDeviceId(user.getId(), deviceId);
        if (existingToken.isPresent()) {
            TokenEntity token = existingToken.get();
            token.setRevoked(false); // Ensure it is unrevoked if re-authenticating
            return token;
        }

        // 3. Device Not Found: Fetch existing sessions sorted from oldest to newest
        List<TokenEntity> userTokens = tokenRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
        int MAX_ALLOWED_DEVICES = 3;

        // 4. Threshold Enforcement: Recycle oldest row if limit is breached
        if (userTokens.size() >= MAX_ALLOWED_DEVICES) {
            TokenEntity oldestToken = userTokens.get(0);

            // CRITICAL SECURITY FIX: Wipe out all credentials tied to the evicted device
            oldestToken.setDeviceId(deviceId);
            oldestToken.setHashAccessToken(null);   // Change field names to match your entity
            oldestToken.setHashRefreshToken(null);  // Change field names to match your entity
            oldestToken.setHashResetKey(null);  // Clear any active reset states
            oldestToken.setRevoked(false);

            // TIMESTAMPS FIX: Reset creation time so it becomes the "newest" in future queries
            if (oldestToken.getCreatedAt() != null) {
                oldestToken.setCreatedAt(Instant.now());
            }

            return oldestToken;
        }

        // 5. Normal Path: Safely allocate a brand new session row
        TokenEntity newToken = new TokenEntity();
        newToken.setUser(user);
        newToken.setDeviceId(deviceId);
        newToken.setRevoked(false);

        // Explicitly set creation timestamp if your entity doesn't handle it via JPA lifecycle hooks
        // newToken.setCreatedAt(LocalDateTime.now());

        return newToken;
    }

    /**
     * Overload for ID-only lookups (e.g. from controllers or token refresh steps)
     */
    public TokenEntity getOrCreateTokenEntityForDevice(Long userId, String deviceId) {
        if (userId == null) {
            throw new CustomException(ErrorMsg.AUTH_004, 404);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_004, 404));

        return getOrCreateTokenEntityForUser(user, deviceId);
    }

    public TokenEntity getOrCreateTokenEntity(UserEntity user) {
        return getOrCreateTokenEntityForUser(user, "DEFAULT_DEVICE");
    }


}
