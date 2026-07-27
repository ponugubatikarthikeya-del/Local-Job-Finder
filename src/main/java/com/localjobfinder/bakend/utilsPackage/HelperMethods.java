package com.localjobfinder.bakend.utilsPackage;

import com.localjobfinder.bakend.OauthPackage.AuthLoginEnum;
import com.localjobfinder.bakend.dbEntity.TokenEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class HelperMethods {

    private final TokenRepository tokenRepository;

    @Autowired
    public HelperMethods(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
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

    // Multi-Device Lookup: Upserts 1 token row per User per DeviceId
    public TokenEntity getOrCreateTokenEntityForDevice(UserEntity user, String deviceId) {
        String targetDeviceId = (deviceId != null && !deviceId.isBlank()) ? deviceId : "DEFAULT_DEVICE";

        Optional<TokenEntity> existingToken = tokenRepository.findByUserAndDeviceId(user, targetDeviceId);

        if (existingToken.isPresent()) {
            return existingToken.get();
        }

        TokenEntity newToken = new TokenEntity();
        newToken.setUser(user);
        newToken.setDeviceId(targetDeviceId);
        return newToken;
    }

    public TokenEntity getOrCreateTokenEntity(UserEntity user) {
        return getOrCreateTokenEntityForDevice(user, "DEFAULT_DEVICE");
    }


}
