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
import com.localjobfinder.bakend.repository.ProfileRepository;
import com.localjobfinder.bakend.responsePackage.*;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtService;
import com.localjobfinder.bakend.repository.TokenRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.sendMailPackage.ForgetEmailSend;
import com.localjobfinder.bakend.serviceInterfacePackage.AuthServiceInterface;
import com.localjobfinder.bakend.utilsPackage.HelperMethods;
import com.localjobfinder.bakend.utilsPackage.RandomUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Service
public class AuthService implements AuthServiceInterface {

    private UserRepository userRepository;
    private JwtService jwtService;
    private ForgetEmailSend forgetEmailSend;
    private PasswordEncoder passwordEncoder;
    private TokenRepository tokenRepository;
    private ModelMapper modelMapper;
    private ProfileRepository profileRepository;
    private OAuthProviderService oAuthProviderService;
    private HelperMethods helperMethods;


    @Autowired
    public void setUserRepository(OAuthProviderService oAuthProviderService
                                              ,UserRepository userRepository
                                             , JwtService jwtService,
                                  ForgetEmailSend forgetEmailSend,
                                  PasswordEncoder passwordEncoder,
                                  TokenRepository tokenRepository,
                                  ModelMapper modelMapper, ProfileRepository profileRepository,
                                  HelperMethods helperMethods)
    {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.forgetEmailSend = forgetEmailSend;
        this .passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
        this.helperMethods = helperMethods;
    }


    @Override
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest, String path, String deviceId) throws CustomException {

        if(deviceId == null || deviceId.isEmpty()){
            deviceId = "DEFAULT_DEVICE";
        }

        Optional<UserEntity> existingUserOpt = userRepository.findByEmail(signupRequest.getEmail());
        UserEntity user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            // If account is already verified, block duplicate signup
            if (AccountStatusEnum.VERIFIED.equals(user.getAccountStatus())) {
                throw new CustomException(ErrorMsg.AUTH_005, 400); // Email already registered
            }
            // If unverified, update details to allow retry
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

        UserEntity savedUser = userRepository.save(user);

        // Generate and dispatch OTP
        Integer otp = RandomUtil.generateOtp();
        long expirationSeconds = 300;

        String emailText = "Hi " + signupRequest.getUsername() + ",\n\n"
                + "Your verification OTP is: " + otp + "\n"
                + "This OTP will expire in 5 minutes.";

        forgetEmailSend.mailSender(signupRequest.getEmail(), emailText, "Signup OTP Verification");

        // Save hashed OTP in tokens table
        TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(savedUser, deviceId);
        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(otp)));
        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(expirationSeconds));
        tokenEntity.setRevoked(false);
        tokenRepository.save(tokenEntity);

        return new AuthResponse(201, SuccessMsg.AUTH_201.name(), SuccessMsg.AUTH_201.getMessage(), path);
    }

    // ==========================================
    // 2. SIGNUP OTP VERIFICATION
    // ==========================================
    @Override
    @Transactional
    public LoginResponse signupOtpVerification(SignupOtpValidationRequest signupOtpValidationRequest, String path,String deviceId) throws CustomException {

        if(deviceId == null || deviceId.isEmpty()){
            deviceId = "DEFAULT_DEVICE";
        }

        if (signupOtpValidationRequest.getOtp() == null || signupOtpValidationRequest.getOtp().isBlank()) {
            throw new CustomException(ErrorMsg.OTP_002, 401);
        }

        UserEntity user = userRepository.findByEmail(signupOtpValidationRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorMsg.OTP_002, 404));

        // Find active matching OTP token
        List<TokenEntity> tokens = tokenRepository.findByUser(user);
        TokenEntity matchedToken = null;

        for (TokenEntity t : tokens) {
            if (t.getOtpExpiry() != null && !t.isRevoked() && t.getOtpExpiry().isAfter(Instant.now())) {
                if (t.getOtpHash() != null && passwordEncoder.matches(signupOtpValidationRequest.getOtp(), t.getOtpHash())) {
                    matchedToken = t;
                    break;
                }
            }
        }

        if (matchedToken == null) {
            throw new CustomException(ErrorMsg.OTP_002, 400); // Invalid or expired OTP
        }

        // Mark Account as Verified
        user.setAccountStatus(AccountStatusEnum.VERIFIED);
        userRepository.save(user);

        ProfilesEntity profile = new ProfilesEntity();
        profile.setUser(user);
        profileRepository.save(profile);

        // Generate JWT Access & Refresh Tokens
        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        // Update token entity from OTP mode to active JWT session mode
        matchedToken.setDeviceId(deviceId);
        matchedToken.setOtpHash(null);
        matchedToken.setOtpExpiry(null);
        matchedToken.setHashAccessToken(jwtAccessToken);
        matchedToken.setHashRefreshToken(jwtRefreshToken);
        matchedToken.setTokenExpiresAt(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 180));
        matchedToken.setRevoked(false);
        tokenRepository.save(matchedToken);

        return new LoginResponse(201, SuccessMsg.AUTH_201.name(), SuccessMsg.AUTH_201.getMessage(), path, jwtAccessToken, jwtRefreshToken);
    }

    // resend otp api call


    @Override
    public AuthResponse resendOtp(EmailCheckupRequest emailCheckupRequest, String path, String deviceId) throws CustomException {

        if(deviceId == null || deviceId.isEmpty()){
            deviceId = "DEFAULT_DEVICE";
        }

        UserEntity user = userRepository.findByEmail(emailCheckupRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_004, 404));

        if (AccountStatusEnum.VERIFIED.equals(user.getAccountStatus())) {
            throw new CustomException(ErrorMsg.AUTH_005, 400); // Already verified
        }

        Integer newOtp = RandomUtil.generateOtp();
        long expirationSeconds = 300;
        String emailText = "Hi " + user.getUsername() + ",\n\n"
                + "Your new verification OTP is: " + newOtp + "\n"
                + "This OTP will expire in 5 minutes.";

        forgetEmailSend.mailSender(user.getEmail(), emailText, "New OTP");

        TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(user, deviceId);
        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(newOtp)));
        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(expirationSeconds));
        tokenEntity.setRevoked(false);
        tokenRepository.save(tokenEntity);

        return new AuthResponse(
                200,
                SuccessMsg.AUTH_204.name(),
                SuccessMsg.AUTH_204.getMessage(),
                path
        );
    }

    // ==========================================
    // 3. STANDARD LOGIN (Email/Password)
    // ==========================================
    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest, String path,String deviceId) throws CustomException {

        if(deviceId == null || deviceId.isEmpty()){
            deviceId = "DEFAULT_DEVICE";
        }

        UserEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_004, 404));

        if (user.getAccountStatus() != AccountStatusEnum.VERIFIED) {

//            // Generate a new fresh OTP for the unverified user
//            Integer newOtp = RandomUtil.generateOtp();
//            long expirationSeconds = 300; // 5 mins
//
//            String emailText = "Hi " + user.getUsername() + ",\n\n"
//                    + "Your account is not verified yet. Your new verification OTP is: " + newOtp + "\n"
//                    + "This OTP will expire in 5 minutes.";
//
//            forgetEmailSend.mailSender(user.getEmail(), emailText, "Signup OTP Verification");
//
//            // Save fresh OTP hash & expiry in token entity
//            TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(user, deviceId);
//            tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(newOtp)));
//            tokenEntity.setOtpExpiry(Instant.now().plusSeconds(expirationSeconds));
//            tokenEntity.setRevoked(false);
//            tokenRepository.save(tokenEntity);

            throw new CustomException(ErrorMsg.AUTH_003, 400); // Unverified account
        }

        // CASE 2 CHECK: Prevent password login if account was registered via Google without a password
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new CustomException(ErrorMsg.AUTH_007, 400); // "Log in with Google or reset password"
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new CustomException(ErrorMsg.AUTH_007, 401); // Bad credentials
        }

        // Issue JWT Tokens
        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        // NEW CODE: Overwrite existing user row on login
        TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(user, deviceId);
        tokenEntity.setHashAccessToken(jwtAccessToken);
        tokenEntity.setHashRefreshToken(jwtRefreshToken);
        tokenEntity.setTokenExpiresAt(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 180));
        tokenEntity.setRevoked(false);
        tokenRepository.save(tokenEntity);

        return new LoginResponse(200, SuccessMsg.USER_200.name(), SuccessMsg.USER_200.getMessage(), path, jwtAccessToken, jwtRefreshToken);
    }

    // ==========================================
    // 4. SOCIAL OAUTH LOGIN & AUTO-LINKING
    // ==========================================
    @Override
    @Transactional
    public LoginResponse socialLogin(OAuthLoginRequest request, String path,String deviceId) throws CustomException {

        if(deviceId == null || deviceId.isEmpty()){
            deviceId = "DEFAULT_DEVICE";
//            throw new CustomException(ErrorMsg.AUTHZ_003,400);
        }

        // 1. Fetch normalized user payload across Google, GitHub, Facebook, etc.
        OAuthUserDto oAuthUser = oAuthProviderService.fetchOAuthUser(request.getProvider(), request.getToken());

        if (oAuthUser.getEmail() == null || oAuthUser.getEmail().isBlank()) {
            throw new CustomException(ErrorMsg.OAUTH_008, 400);
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

            // Safely create empty user profile
            ProfilesEntity profile = new ProfilesEntity();
            profile.setUser(user);
            profileRepository.save(profile);
        }

        // 2. Issue System JWT Access & Refresh Tokens
        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        // NEW CODE: Overwrite existing user row on OAuth login
        TokenEntity tokenEntity = helperMethods.getOrCreateTokenEntityForDevice(user, deviceId);
        tokenEntity.setHashAccessToken(jwtAccessToken);
        tokenEntity.setHashRefreshToken(jwtRefreshToken);
        tokenEntity.setTokenExpiresAt(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 180));
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

//    // Helper method to keep provider-linking logic modular and clean
//    private void linkProviderId(UserEntity user, AuthLoginEnum provider, String providerId) {
//        if (providerId == null || providerId.isBlank()) return;
//
//        switch (provider) {
//            case GOOGLE -> {
//                if (user.getGoogleId() == null) user.setGoogleId(providerId);
//            }
//            case GITHUB -> {
//                if (user.getGithubId() == null) user.setGithubId(providerId);
//            }
//            case FACEBOOK -> {
//                if (user.getFacebookId() == null) user.setFacebookId(providerId);
//            }
//            default -> { /* LOCAL or HYBRID don't have provider IDs */}
//        }
//    }


//    @Override
//    @Transactional
//    public AuthResponse signup(SignupRequest signupRequest, String path) throws CustomException{
//
//        // validation is expected to be handled by @Valid on controller; double check basic assumptions
//        Optional<UserEntity> userEntity = userRepository.findByEmail(signupRequest.getEmail());
//
//        if(userEntity.isPresent()){
//            throw new CustomException(ErrorMsg.AUTH_005,400);
//        }
//
//        UserEntity user = new UserEntity();
//
//        Integer otp = RandomUtil.generateOtp();
//
//        long expiration = 300 ;
//
//        String text = "Hi " + signupRequest.getUsername() + ",\n\n"
//                + "Here is your signup verification  OTP: " + otp + "\n"
//                + "This OTP will expire in " + expiration/60 + " minutes.";
//
//        // send email asynchronously
//        forgetEmailSend.mailSender(signupRequest.getEmail(),text,"signup");
//
//        user.setUsername(signupRequest.getUsername());
//        user.setPhone(signupRequest.getPhone());
//        user.setEmail(signupRequest.getEmail());
//        user.setAccountStatus(AccountStatusEnum.UNVERIFIED);
//
//        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
//
//        user.setPasswordHash(encodedPassword);
//
//
//        UserEntity savedUser = userRepository.save(user);
//
//        // store hashed otp in tokens table
//        TokenEntity tokenEntity = new TokenEntity();
//        tokenEntity.setUser(savedUser);
//        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(otp)));
//        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(expiration));
//        tokenEntity.setRevoked(false);
//        refreshTokenRepository.save(tokenEntity);
//
//       return new AuthResponse(
//               201,
//               SuccessMsg.AUTH_201.name(),
//               SuccessMsg.AUTH_201.getMessage(),
//               path
//       );
//    }
//
//    // signup otp verification
//
//    @Override
//    @Transactional
//    public LoginResponse signupOtpVerification(SignupOtpValidationRequest signupOtpValidationRequest, String request) throws CustomException {
//
//        if(signupOtpValidationRequest.getOtp() == null){
//            throw new CustomException(ErrorMsg.OTP_002,401);
//        }
//
//        Optional<UserEntity> userEntity = userRepository.findByEmail(signupOtpValidationRequest.getEmail());
//
//        if(userEntity.isEmpty()){
//            throw new CustomException(ErrorMsg.OTP_002,404);
//        }
//
//        UserEntity user = userEntity.get();
//
//        // find stored OTP token for user
//        List<TokenEntity> tokens = refreshTokenRepository.findByUser(user);
//        TokenEntity matched = null;
//        for (TokenEntity t : tokens) {
//            if (t.getOtpExpiry() != null && !t.isRevoked() && t.getOtpExpiry().isAfter(Instant.now())) {
//                String providedOtpStr = signupOtpValidationRequest.getOtp();
//                if (t.getOtpHash() != null && passwordEncoder.matches(providedOtpStr, t.getOtpHash())) {
//                    matched = t;
//                    break;
//                }
//            }
//        }
//
//        if (matched == null) {
//            throw new CustomException(ErrorMsg.OTP_002,400);
//        }
//
//        user.setAccountStatus(AccountStatusEnum.VERIFIED);
//
////        // PRODUCTION STEP: Bind device token immediately upon successful validation if provided
////        if (signupOtpValidationRequest.getFcmToken() != null && !signupOtpValidationRequest.getFcmToken().isBlank()) {
////            user.setFcmToken(signupOtpValidationRequest.getFcmToken());
////        }
//
//        userRepository.save(user);
//
//
//        ProfilesEntity profilesEntity = new ProfilesEntity();
//        profilesEntity.setUser(user);
//
//        profileRepository.save(profilesEntity);
//
//        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
//        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());
//
//        matched.setOtpHash(null);
//        matched.setOtpExpiry(null);
//        matched.setHashAccessToken(jwtAccessToken);
//        matched.setHashRefreshToken(jwtRefreshToken);
//        matched.setRevoked(false);
//
//        refreshTokenRepository.save(matched);
//
//        return new LoginResponse(
//                201,
//                SuccessMsg.AUTH_201.name(),
//                SuccessMsg.AUTH_201.getMessage(),
//                request,
//                jwtAccessToken,
//                jwtRefreshToken
//        );
//
//    }
//
//    // login service
//
//    @Override
//    @Transactional
//    public LoginResponse login(LoginRequest loginRequest, String path) throws CustomException {
//
//        Optional<UserEntity> userEntity = userRepository.findByEmail(loginRequest.getEmail());
//
//        if(userEntity.isEmpty()){
//            throw new CustomException(ErrorMsg.AUTH_004,404);
//        }
//
//        if(userEntity.get().getAccountStatus() != AccountStatusEnum.VERIFIED){
//            throw new CustomException(ErrorMsg.AUTH_003,400);
//        }
//
//        UserEntity user = userEntity.get();
//        boolean result = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());
//
//        if(!result){
//            throw new CustomException(ErrorMsg.AUTH_007,401);
//        }
//
////        // PRODUCTION STEP: Update token during authentication if user switched phones or reinstalled the app
////        if (loginRequest.getFcmToken() != null && !loginRequest.getFcmToken().isBlank()) {
////            user.setFcmToken(loginRequest.getFcmToken());
////            userRepository.save(user); // Flushes fresh token routing strings to database indices
////        }
//
//        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
//        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());
//
//        // Persist hashed refresh token in dedicated table
//        TokenEntity tokenEntity = new TokenEntity();
//        tokenEntity.setUser(user);
//
//        tokenEntity.setHashAccessToken(jwtAccessToken);
//        tokenEntity.setHashRefreshToken(jwtRefreshToken);
//
//        tokenEntity.setRevoked(false);
//        refreshTokenRepository.save(tokenEntity);
//
//        return new LoginResponse(
//                200,
//                SuccessMsg.USER_200.name(),
//                SuccessMsg.USER_200.getMessage(),
//                path,
//                jwtAccessToken,
//                jwtRefreshToken
//        );
//
//    }

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
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_006, 401));

        // find candidate refresh tokens for user and match by hash
        List<TokenEntity> tokens = tokenRepository.findByUser(user);
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
            throw new CustomException(ErrorMsg.AUTH_006, 401);
        }

        // NEW CODE: Overwrite the existing row in-place with the rotated tokens
        String newJwtRefreshToken = jwtService.generateRefreshToken(user.getId());
        String newJwtAccessToken = jwtService.generateAccessToken(user.getId());

        matched.setHashAccessToken(newJwtAccessToken);
        matched.setHashRefreshToken(newJwtRefreshToken);
        matched.setTokenExpiresAt(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 180));
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
    @Transactional
    public AuthResponse forgetPasswordEmailRequest(
            EmailCheckupRequest emailCheckupRequest, String request) throws CustomException {

        Optional<UserEntity> userEntity = userRepository.findByEmail(emailCheckupRequest.getEmail());

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_008,404);
        }

        UserEntity user = userEntity.get();

        Integer otp = RandomUtil.generateOtp();

        long expiration = 300 ;

        String text = "Hi " + user.getUsername() + ",\n\n"
                + "Here is your reset password OTP: " + otp + "\n"
                + "This OTP will expire in " + expiration + " seconds.\n\n"
                + "If you did not request a password reset, please ignore this email.";

        // send email asynchronously
        forgetEmailSend.mailSender(user.getEmail(),text,"password reset");

        // NEW CODE: Reuses user's single token row
        List<TokenEntity> userTokens = tokenRepository.findByUser(user);
        TokenEntity tokenEntity = userTokens.isEmpty()
                ? helperMethods.getOrCreateTokenEntity(user)
                : userTokens.getFirst();

        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(otp)));
        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(expiration));
        tokenEntity.setRevoked(false);
        tokenRepository.save(tokenEntity);

        return new AuthResponse(
                200,
                SuccessMsg.AUTH_204.name(),
                SuccessMsg.AUTH_204.getMessage(),
                request
        );
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
        List<TokenEntity> tokens = tokenRepository.findByUser(user);
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
}
