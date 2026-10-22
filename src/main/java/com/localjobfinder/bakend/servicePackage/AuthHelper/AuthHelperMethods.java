package com.localjobfinder.bakend.servicePackage.AuthHelper;

import com.localjobfinder.bakend.apiDataPackage.SignupRequest;
import com.localjobfinder.bakend.dbEntity.TokenEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.enums.AccountStatusEnum;
import com.localjobfinder.bakend.enums.AuthLoginEnum;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.repository.TokenRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import com.sun.jdi.request.DuplicateRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class AuthHelperMethods {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HelperMethods helperMethods;
    private final TokenRepository tokenRepository;

    @Autowired
    public AuthHelperMethods(UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             HelperMethods helperMethods,TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.helperMethods = helperMethods;
        this.tokenRepository = tokenRepository;
    }

    // Dedicated internal transaction wrapper to guarantee database efficiency
    @Transactional
    public UserEntity saveUserAndOtpTransaction(SignupRequest signupRequest, String targetDeviceId, Integer otp, long otpExpirationSeconds) {

        Optional<UserEntity> existingUserOpt = userRepository.findByEmail(signupRequest.getEmail());

        UserEntity user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            // Allow retry only for UNVERIFIED accounts
            if (AccountStatusEnum.VERIFIED.equals(user.getAccountStatus())) {
                throw new DuplicateRequestException(ErrorMsg.AUTH_005.getMessage()); // Email already verified
            }
            // Update unverified user details to support retry flows cleanly
            user.setUsername(signupRequest.getUsername());
            user.setPhone(signupRequest.getPhone());
            user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));

        } else {

            // Fresh registration
            user = new UserEntity();
            user.setUsername(signupRequest.getUsername());
            user.setPhone(signupRequest.getPhone());
            user.setEmail(signupRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));
            user.setAccountStatus(AccountStatusEnum.UNVERIFIED);
            user.setAuthProvider(AuthLoginEnum.LOCAL);

        }

        // Save user and capture the returned reference instantly
        UserEntity savedUser = userRepository.save(user);

        // Persist hashed OTP token record safely
        TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(savedUser.getId(), targetDeviceId);
        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(otp)));
        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(otpExpirationSeconds));
        tokenEntity.setRevoked(false);

        tokenRepository.save(tokenEntity);

        return savedUser;
    }


    // Separate transactional database operations to keep the connection lifespan ultra-short
    @Transactional
    public void saveOtpTokenTransaction(Long userId, String targetDeviceId, Integer newOtp, long otpExpirationSeconds) {
        TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(userId, targetDeviceId);
        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(newOtp)));
        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(otpExpirationSeconds)); // Simplified time syntax
        tokenEntity.setRevoked(false);
        tokenRepository.save(tokenEntity);
    }


    // Dedicated short-lived transactional block for token management
    @Transactional
    public void saveForgotPasswordTokenTransaction(UserEntity user, Integer otp, long expiration) {
        // Reuses user's single token row safely
        List<TokenEntity> userTokens = tokenRepository.findByUserId(user.getId());

        TokenEntity tokenEntity = userTokens.isEmpty()
                ? helperMethods.getOrCreateTokenEntity(user)
                : userTokens.get(0);

        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(otp)));
        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(expiration));
        tokenEntity.setRevoked(false);

        tokenRepository.save(tokenEntity);
    }


}
