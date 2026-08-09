package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.OauthPackage.OAuthLoginRequest;
import com.localjobfinder.bakend.OauthPackage.OAuthProviderService;
import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.dbEntity.ProfilesEntity;
import com.localjobfinder.bakend.dbEntity.TokenEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.OauthPackage.OAuthUserDto;
import com.localjobfinder.bakend.enums.AccountStatusEnum;
import com.localjobfinder.bakend.OauthPackage.AuthLoginEnum;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.FcmTokenRepository;
import com.localjobfinder.bakend.repository.ProfileRepository;
import com.localjobfinder.bakend.responsePackage.*;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtService;
import com.localjobfinder.bakend.repository.TokenRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.sendMailPackage.EmailSend;
import com.localjobfinder.bakend.serviceInterfacePackage.AuthServiceInterface;
import com.localjobfinder.bakend.utilsPackage.HelperMethods;
import com.localjobfinder.bakend.utilsPackage.RandomUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;


@Service
public class AuthService implements AuthServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private UserRepository userRepository;
    private JwtService jwtService;
    private EmailSend emailSend;
    private PasswordEncoder passwordEncoder;
    private TokenRepository tokenRepository;
    private ModelMapper modelMapper;
    private ProfileRepository profileRepository;
    private OAuthProviderService oAuthProviderService;
    private HelperMethods helperMethods;
    private FcmTokenRepository fcmTokenRepository;


    @Autowired
    public void setUserRepository(OAuthProviderService oAuthProviderService
            , UserRepository userRepository
            , JwtService jwtService,
                                  EmailSend emailSend,
                                  PasswordEncoder passwordEncoder,
                                  TokenRepository tokenRepository,
                                  ModelMapper modelMapper, ProfileRepository profileRepository,
                                  HelperMethods helperMethods,
                                  FcmTokenRepository fcmTokenRepository) {
        this.oAuthProviderService = oAuthProviderService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.emailSend = emailSend;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
        this.helperMethods = helperMethods;
        this.fcmTokenRepository = fcmTokenRepository;
    }


    @Override
// 1. REMOVE @Transactional here to protect network threads from blocking database connection pools
    public AuthResponse signup(SignupRequest signupRequest, String path, String deviceId) throws CustomException {

        String targetDeviceId = (deviceId != null && !deviceId.isBlank()) ? deviceId : "DEFAULT_DEVICE";

        Integer otp = RandomUtil.generateOtp();
        long otpExpirationSeconds = 300; // 5 minutes

        // 2. Perform all database persistence tasks first inside a short-lived transaction block
        // This captures the saved user directly without needing a second findByEmail query!
        UserEntity savedUser = this.saveUserAndOtpTransaction(signupRequest, targetDeviceId, otp, otpExpirationSeconds);

        // 3. Dispatch the network API call safely OUTSIDE the database transaction boundary
        try {
            String emailSubject = "Email Verification OTP";
            String emailText = "Hi " + savedUser.getUsername() + ",\n\n"
                    + "Your verification OTP is: " + otp + "\n"
                    + "This OTP will expire in 5 minutes.\n\n"
                    + "Please do not share this OTP with anyone.";

            emailSend.mailSender(savedUser.getEmail(), emailText, emailSubject);
        } catch (Exception e) {
            throw new CustomException(ErrorMsg.SYS_002, 500); // Email send failed
        }

        return new AuthResponse(201, SuccessMsg.AUTH_201.name(), SuccessMsg.AUTH_201.getMessage(), path);
    }

    // Dedicated internal transaction wrapper to guarantee database efficiency
    @Transactional
    protected UserEntity saveUserAndOtpTransaction(SignupRequest signupRequest, String targetDeviceId, Integer otp, long otpExpirationSeconds) throws CustomException {

        Optional<UserEntity> existingUserOpt = userRepository.findByEmail(signupRequest.getEmail());
        UserEntity user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            // Allow retry only for UNVERIFIED accounts
            if (AccountStatusEnum.VERIFIED.equals(user.getAccountStatus())) {
                throw new CustomException(ErrorMsg.AUTH_005, 400); // Email already verified
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


    // 2. SIGNUP OTP VERIFICATION ....................................................................

    @Override
    @Transactional
    public LoginResponse signupOtpVerification(SignupOtpValidationRequest signupOtpValidationRequest, String path, String deviceId) throws CustomException {

        String targetDeviceId = (deviceId != null && !deviceId.isBlank()) ? deviceId : "DEFAULT_DEVICE";

        if (signupOtpValidationRequest.getOtp() == null || signupOtpValidationRequest.getOtp().isBlank()) {
            throw new CustomException(ErrorMsg.OTP_002, 400);
        }

        UserEntity user = userRepository.findByEmail(signupOtpValidationRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_004, 404));

        // 1. Fetch tokens using UserEntity directly
        List<TokenEntity> tokens = tokenRepository.findByUserId(user.getId());

        TokenEntity matchedToken = null;

        Instant now = Instant.now();

        for (TokenEntity t : tokens) {
            if (t.getOtpExpiry() != null && !t.isRevoked() && t.getOtpExpiry().isAfter(now)) {
                if (t.getOtpHash() != null && passwordEncoder.matches(signupOtpValidationRequest.getOtp(), t.getOtpHash())) {
                    matchedToken = t;
                    break;
                }
            }
        }


        if (matchedToken == null) {
            throw new CustomException(ErrorMsg.OTP_002, 400); // Invalid or expired OTP
        }

        if(!matchedToken.getDeviceId().equals(targetDeviceId)){
            matchedToken.setDeviceId(targetDeviceId);
        }

        // 2. Mark Account as Verified
        user.setAccountStatus(AccountStatusEnum.VERIFIED);
        userRepository.save(user);

        // 3. Create User Profile if it doesn't already exist (Prevents Duplicate Key 500 Error)
        if (profileRepository.findByUserId(user.getId()).isEmpty()) {
            ProfilesEntity profile = new ProfilesEntity();
            profile.setUser(user);
            profileRepository.save(profile);
        }

        // 4. Generate JWT Access & Refresh Tokens
        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        // 5. Update token entity from OTP mode to active JWT session mode
        matchedToken.setDeviceId(targetDeviceId);
        matchedToken.setOtpHash(null);
        matchedToken.setOtpExpiry(null);
        matchedToken.setHashAccessToken(jwtAccessToken);
        matchedToken.setHashRefreshToken(jwtRefreshToken);
        matchedToken.setTokenExpiresAt(now.plus(180, ChronoUnit.DAYS)); // Clean Instant duration calculation
        matchedToken.setRevoked(false);

        tokenRepository.save(matchedToken);

        return new LoginResponse(
                201,
                SuccessMsg.AUTH_201.name(),
                SuccessMsg.AUTH_201.getMessage(),
                path,
                jwtAccessToken,
                jwtRefreshToken
        );
    }


    @Override
// 1. REMOVE @Transactional here to keep your server threads and connection pools safe from Network I/O
    public AuthResponse resendOtp(EmailCheckupRequest emailCheckupRequest, String path, String deviceId) throws CustomException {

        String targetDeviceId = (deviceId != null && !deviceId.isBlank()) ? deviceId : "DEFAULT_DEVICE";

        // 2. Fetch the user details immediately
        UserEntity user = userRepository.findByEmail(emailCheckupRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_004, 404));

        // 3. UNCOMMENT THIS: Prevent spamming already verified users
        if (AccountStatusEnum.VERIFIED.equals(user.getAccountStatus())) {
            throw new CustomException(ErrorMsg.AUTH_005, 400); // Already verified
        }

        Integer newOtp = RandomUtil.generateOtp();
        long otpExpirationSeconds = 300; // 5 minutes

        // 4. Save the OTP to the database FIRST inside an isolated transaction
        this.saveOtpTokenTransaction(user.getId(), targetDeviceId, newOtp, otpExpirationSeconds);

        // 5. Send the email safely AFTER the database operation is guaranteed successful
        try {
            String emailSubject = "Resend Email Verification OTP";
            String emailText = "Hi " + user.getUsername() + ",\n\n"
                    + "Your new verification OTP is: " + newOtp + "\n"
                    + "This OTP will expire in 5 minutes.\n\n"
                    + "Please do not share this OTP with anyone.";

            emailSend.mailSender(user.getEmail(), emailText, emailSubject);
        } catch (Exception e) {
            // Log your error here
            throw new CustomException(ErrorMsg.SYS_002, 500); // Email send failed
        }

        return new AuthResponse(
                200,
                SuccessMsg.AUTH_204.name(),
                SuccessMsg.AUTH_204.getMessage(),
                path
        );
    }

    // Separate transactional database operations to keep the connection lifespan ultra-short
    @Transactional
    protected void saveOtpTokenTransaction(Long userId, String targetDeviceId, Integer newOtp, long otpExpirationSeconds) {
        TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(userId, targetDeviceId);
        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(newOtp)));
        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(otpExpirationSeconds)); // Simplified time syntax
        tokenEntity.setRevoked(false);
        tokenRepository.save(tokenEntity);
    }


    // ==========================================
    // 3. STANDARD LOGIN (Email/Password)
    // ==========================================
    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest, String path, String deviceId) throws CustomException {

        String targetDeviceId = (deviceId != null && !deviceId.isBlank()) ? deviceId : "DEFAULT_DEVICE";

        UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_004, 404));

        if (user.getId() == null) {
            throw new CustomException(ErrorMsg.GEN_001, 500); // User ID is null - should not happen
        }

        if (user.getAccountStatus() != AccountStatusEnum.VERIFIED) {
            throw new CustomException(ErrorMsg.AUTH_003, 400); // Unverified account
        }

        // Prevent password login if account was registered via OAuth without a password
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new CustomException(ErrorMsg.AUTH_007, 400); // Use OAuth login instead
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorMsg.AUTH_007, 401); // Bad credentials
        }



        // Generate JWT Tokens
        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        // Save tokens for device
        TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(user.getId(), targetDeviceId);
        tokenEntity.setHashAccessToken(jwtAccessToken);
        tokenEntity.setHashRefreshToken(jwtRefreshToken);
        tokenEntity.setTokenExpiresAt(Instant.now().plus(180, ChronoUnit.DAYS));
        tokenEntity.setRevoked(false);
        tokenRepository.save(tokenEntity);

        return new LoginResponse(200,
                SuccessMsg.USER_200.name(),
                SuccessMsg.USER_200.getMessage(),
                path,
                jwtAccessToken,
                jwtRefreshToken);
    }


    // 4. SOCIAL OAUTH LOGIN & AUTO-LINKING .............................................................

    @Override
    @Transactional
    public LoginResponse socialLogin(OAuthLoginRequest request, String path,String deviceId) throws CustomException {

        if(deviceId == null || deviceId.isEmpty()){
            deviceId = "DEFAULT_DEVICE";
//            throw new CustomException(ErrorMsg.AUTHZ_003,400);
        }

        // 1. Fetch normalized user payload across Google, GitHub, Facebook, etc.
        OAuthUserDto oAuthUser = oAuthProviderService.fetchOAuthUser(request.getProvider(), request.getToken());

        if (oAuthUser == null || oAuthUser.getProviderName() == null) {
            throw new CustomException(ErrorMsg.OAUTH_004, 400);
        }

        if (oAuthUser.getEmail() == null || oAuthUser.getEmail().isBlank()) {
            throw new CustomException(ErrorMsg.OAUTH_008, 400);
        }

        if(!oAuthUser.isEmailVerified()){
            throw new CustomException(ErrorMsg.OAUTH_004, 400);
        }

        AuthLoginEnum providerEnum = oAuthUser.getProviderName(); // Assuming oAuthUser returns AuthLoginEnum
        Optional<UserEntity> userOpt = userRepository.findByEmail(oAuthUser.getEmail());
        UserEntity user;

        if (userOpt.isPresent()) {
            user = userOpt.get();

            // AUTO-LINKING: Dynamically map the Provider ID to the correct column
            helperMethods.linkProviderId(user, providerEnum, oAuthUser.getProviderId());

            // Upgrade LOCAL-only account to HYBRID if they log in via social auth
            if (AuthLoginEnum.LOCAL.equals(user.getAuthProvider())) {
                user.setAuthProvider(AuthLoginEnum.HYBRID);
            }

            // Auto-verify account status upon successful social auth
            user.setAccountStatus(AccountStatusEnum.VERIFIED);
            userRepository.save(user);

        } else {
            // CASE 1: First-time signup via Social Provider
            user = new UserEntity();
            user.setEmail(oAuthUser.getEmail());
            user.setUsername(oAuthUser.getName() != null ? oAuthUser.getName() : oAuthUser.getEmail().split("@")[0]);
            user.setAuthProvider(providerEnum);
            user.setPasswordHash(null); // Social signups do not require local passwords
            user.setAccountStatus(AccountStatusEnum.VERIFIED);

            // Dynamically set Provider ID
            helperMethods.linkProviderId(user, providerEnum, oAuthUser.getProviderId());

            user = userRepository.save(user);
        }

        // Safe Profile Creation
        if (profileRepository.findByUserId(user.getId()).isEmpty()) {
            ProfilesEntity profile = new ProfilesEntity();
            profile.setUser(user);
            profileRepository.save(profile);
        }

        // 2. Issue System JWT Access & Refresh Tokens
        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        // NEW CODE: Overwrite existing user row on OAuth login
        TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(user.getId(), deviceId);
        tokenEntity.setHashAccessToken(jwtAccessToken);
        tokenEntity.setHashRefreshToken(jwtRefreshToken);
        tokenEntity.setTokenExpiresAt(Instant.now().plus(180, ChronoUnit.DAYS));
        tokenEntity.setRevoked(false);
        tokenRepository.save(tokenEntity);

        return new LoginResponse(
                200,
                SuccessMsg.USER_200.name(),
                providerEnum.name() + " Authentication Successful",
                path,
                jwtAccessToken,
                jwtRefreshToken
        );
    }


    @Override
    @Transactional
    public RefreshTokenResponse newAccessToken(Long userId, String refreshToken,String path) throws CustomException {

        if (userId == null) {
            throw new CustomException(ErrorMsg.AUTH_006, 401);
        }

        // 1. Cryptographic Check: Immediately reject if the JWT string string itself is expired
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new CustomException(ErrorMsg.TOKEN_002, 401); // Token expired message
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008, 401));

        // find candidate refresh tokens for user and match by hash
        List<TokenEntity> tokens = tokenRepository.findByUserId(user.getId());
        TokenEntity matched = null;

        // 2. Database Expiration Check Loop
        for (TokenEntity t : tokens) {
            if (!t.isRevoked()) {
                // Check matching hash AND ensure current time hasn't passed database expiration record
                if (refreshToken.equals(t.getHashRefreshToken()) &&
                        t.getTokenExpiresAt() != null && t.getTokenExpiresAt().isAfter(Instant.now())) {
                    matched = t;
                    break;
                }
            }
        }

        if (matched == null) {
            throw new CustomException(ErrorMsg.AUTH_008, 401);
        }

        // NEW CODE: Overwrite the existing row in-place with the rotated tokens
        String newJwtRefreshToken = jwtService.generateRefreshToken(user.getId());
        String newJwtAccessToken = jwtService.generateAccessToken(user.getId());

        matched.setHashAccessToken(newJwtAccessToken);
        matched.setHashRefreshToken(newJwtRefreshToken);
        matched.setTokenExpiresAt(Instant.now().plus(180, ChronoUnit.DAYS));
        matched.setRevoked(false); // Reset active status
        tokenRepository.save(matched);

        return new RefreshTokenResponse(
                200,
                SuccessMsg.AUTH_202.name(),
                SuccessMsg.GEN_200.getMessage(),
                path,
                newJwtAccessToken,
                newJwtRefreshToken
        );

    }

    // email check up for reset password

    @Override
// REMOVE @Transactional here to protect network threads from blocking database connections
    public AuthResponse forgetPasswordEmailRequest(
            EmailCheckupRequest emailCheckupRequest, String request) throws CustomException {

        // 1. Fetch user details immediately
        UserEntity user = userRepository.findByEmail(emailCheckupRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008, 404));

        // CRUCIAL SECURITY GUARD: Block unverified users from requesting a password reset
        if (AccountStatusEnum.UNVERIFIED.equals(user.getAccountStatus())) {
            throw new CustomException(ErrorMsg.AUTH_003, 400); // "Account is not verified. Please verify first."
        }

        Integer otp = RandomUtil.generateOtp();
        long expiration = 300; // 5 minutes

        // 2. Persist the OTP token data FIRST inside a short-lived isolated transaction
        this.saveForgotPasswordTokenTransaction(user, otp, expiration);

        // 3. Dispatch the email safely OUTSIDE the active database transaction boundary
        try {
            String text = "Hi " + user.getUsername() + ",\n\n"
                    + "Here is your reset password OTP: " + otp + "\n"
                    + "This OTP will expire in 5 minutes.\n\n"
                    + "If you did not request a password reset, please ignore this email.";

            emailSend.mailSender(user.getEmail(), text, "password reset");
        } catch (Exception e) {
            throw new CustomException(ErrorMsg.SYS_002, 500); // Email send failed
        }

        return new AuthResponse(
                200,
                SuccessMsg.AUTH_204.name(),
                SuccessMsg.AUTH_204.getMessage(),
                request
        );
    }

    // Dedicated short-lived transactional block for token management
    @Transactional
    protected void saveForgotPasswordTokenTransaction(UserEntity user, Integer otp, long expiration) {
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


    // reset password otp validation

    @Override
    @Transactional
    public ResetKeyOtpResponse forgetPasswordOtpValidate(
            ResetPasswordOtpValidateRequest resetPasswordOtpValidateRequest, String request) throws CustomException {

        if(resetPasswordOtpValidateRequest.getOtp() <= 0){
            throw new CustomException(ErrorMsg.OTP_002,401);
        }

        Optional<UserEntity> userEntity = userRepository.findByEmail(resetPasswordOtpValidateRequest.getEmail());

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.OTP_002,404);
        }

        UserEntity user = userEntity.get();

        // find stored OTP token for user
        List<TokenEntity> tokens = tokenRepository.findByUserId(user.getId());
        TokenEntity matched = null;

        for (TokenEntity t : tokens) {
            if (t.getOtpExpiry() != null && !t.isRevoked() && t.getOtpExpiry().isAfter(Instant.now())) {
                String providedOtpStr = String.valueOf(resetPasswordOtpValidateRequest.getOtp());
                if (t.getOtpHash() != null && passwordEncoder.matches(providedOtpStr, t.getOtpHash())) {
                    matched = t;
                    break;
                }
            }
        }

        if (matched == null) {
            throw new CustomException(ErrorMsg.OTP_002,400);
        }

        String resetKey = RandomUtil.randomString();

       // OVERWRITE IN PLACE: Clear OTP and attach the reset key to the SAME row
        matched.setHashResetKey(resetKey);
        matched.setOtpHash(null);
        matched.setOtpExpiry(null);

        tokenRepository.save(matched);

        return new ResetKeyOtpResponse(
                200,
                SuccessMsg.OTP_200.name(),
                SuccessMsg.OTP_200.getMessage(),
                request,
                resetKey
        );

    }

    // password change logic

    @Override
    @Transactional
    public AuthResponse forgetPasswordRequest(
            PasswordRequest passwordRequest, String request) throws CustomException {

        //if reset password and confirm password does not match then throw mismatch Exception
        if(!passwordRequest.getResetPassword().equals(passwordRequest.getConfirmPassword())){
            throw new CustomException(ErrorMsg.AUTH_009,400);
        }

        // find token by reset key
        Optional<TokenEntity> tokenEntityOptional = tokenRepository.findByHashResetKey(passwordRequest.getResetKey());
        if (tokenEntityOptional.isEmpty()) {
            throw new CustomException(ErrorMsg.AUTH_008,404);
        }

        TokenEntity tokenEntity = tokenEntityOptional.get();
        UserEntity user = tokenEntity.getUser();

        user.setPasswordHash(passwordEncoder.encode(passwordRequest.getResetPassword()));
        userRepository.save(user);

        // CLEANUP: Nullify the reset key so it becomes single-use
        tokenEntity.setHashResetKey(null);
        tokenRepository.save(tokenEntity);

        // OPTIONAL (Recommended Security Practice):
        // Un-comment below to revoke all old sessions so the user must log in with their new password:
        // tokenRepository.deleteByUser(user);

        return new AuthResponse(
                201,
                SuccessMsg.AUTH_205.name(),
                SuccessMsg.AUTH_205.getMessage(),
                request
        );
    }

    @Override
    @Transactional
    public AuthResponse logout(Long userId, String deviceId, boolean allDevices, String path,FcmTokenRequest fcmTokenRequest) throws CustomException {

        if(userId == null){
            throw new CustomException(ErrorMsg.AUTH_008,404);
        }

        // Check if the user exists quickly without pulling the whole object graph
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorMsg.AUTH_008, 404);
        }

        if (fcmTokenRequest == null || fcmTokenRequest.getDeviceFcmToken() == null || path == null) {
            throw new CustomException(ErrorMsg.TOKEN_002, 400);
        }

        fcmTokenRepository.deleteByUserIdAndDeviceToken(userId, fcmTokenRequest.getDeviceFcmToken());

        log.info("Successfully removed FCM token on logout for userId={}", userId);

        if (allDevices) {
            // Option 2: Remove every single device session row from DB
            tokenRepository.deleteByUserId(userId);
            return new AuthResponse(
                    200,
                    SuccessMsg.AUTH_203.name(),
                    SuccessMsg.AUTH_203.getMessage(),
                    path
            );
        }

        // Option 1: Remove only the current device session
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "DEFAULT_DEVICE";
        }

        tokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);

        return new AuthResponse(
                200,
                SuccessMsg.AUTH_203.name(),
                SuccessMsg.AUTH_203.getMessage(),
               path
        );
    }
}
