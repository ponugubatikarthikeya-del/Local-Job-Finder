package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.dbEntity.ProfilesEntity;
import com.localjobfinder.bakend.dbEntity.TokenEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.enums.AccountStatusEnum;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.ProfileRepository;
import com.localjobfinder.bakend.responsePackage.*;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtService;
import com.localjobfinder.bakend.repository.RefreshTokenRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.sendMailPackage.ForgetEmailSend;
import com.localjobfinder.bakend.serviceInterfacePackage.AuthServiceInterface;
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
    private RefreshTokenRepository refreshTokenRepository;
    private ModelMapper modelMapper;
    private ProfileRepository profileRepository;


    @Autowired
    public void setUserRepository(UserRepository userRepository
                                             , JwtService jwtService,
                                  ForgetEmailSend forgetEmailSend,
                                  PasswordEncoder passwordEncoder,
                                  RefreshTokenRepository refreshTokenRepository, ModelMapper modelMapper, ProfileRepository profileRepository)
    {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.forgetEmailSend = forgetEmailSend;
        this .passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest, String path) throws CustomException{

        // validation is expected to be handled by @Valid on controller; double check basic assumptions
        Optional<UserEntity> userEntity = userRepository.findByEmail(signupRequest.getEmail());

        if(userEntity.isPresent()){
            throw new CustomException(ErrorMsg.AUTH_005,400);
        }

        UserEntity user = new UserEntity();

        Integer otp = RandomUtil.generateOtp();

        long expiration = 300 ;

        String text = "Hi " + signupRequest.getUsername() + ",\n\n"
                + "Here is your signup verification  OTP: " + otp + "\n"
                + "This OTP will expire in " + expiration/60 + " minutes.";

        // send email asynchronously
        forgetEmailSend.mailSender(signupRequest.getEmail(),text,"signup");

        user.setUsername(signupRequest.getUsername());
        user.setPhone(signupRequest.getPhone());
        user.setEmail(signupRequest.getEmail());
        user.setAccountStatus(AccountStatusEnum.UNVERIFIED);

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        user.setPasswordHash(encodedPassword);


        UserEntity savedUser = userRepository.save(user);

        // store hashed otp in tokens table
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setUser(savedUser);
        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(otp)));
        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(expiration));
        tokenEntity.setRevoked(false);
        refreshTokenRepository.save(tokenEntity);

       return new AuthResponse(
               201,
               SuccessMsg.AUTH_201.name(),
               SuccessMsg.AUTH_201.getMessage(),
               path
       );
    }

    // signup otp verification

    @Override
    @Transactional
    public LoginResponse signupOtpVerification(SignupOtpValidationRequest signupOtpValidationRequest, String request) throws CustomException {

        if(signupOtpValidationRequest.getOtp() <= 0){
            throw new CustomException(ErrorMsg.OTP_002,401);
        }

        Optional<UserEntity> userEntity = userRepository.findByEmail(signupOtpValidationRequest.getEmail());

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.OTP_002,404);
        }

        UserEntity user = userEntity.get();

        // find stored OTP token for user
        List<TokenEntity> tokens = refreshTokenRepository.findByUser(user);
        TokenEntity matched = null;
        for (TokenEntity t : tokens) {
            if (t.getOtpExpiry() != null && !t.isRevoked() && t.getOtpExpiry().isAfter(Instant.now())) {
                String providedOtpStr = String.valueOf(signupOtpValidationRequest.getOtp());
                if (t.getOtpHash() != null && passwordEncoder.matches(providedOtpStr, t.getOtpHash())) {
                    matched = t;
                    break;
                }
            }
        }

        if (matched == null) {
            throw new CustomException(ErrorMsg.OTP_002,400);
        }

        user.setAccountStatus(AccountStatusEnum.VERIFIED);

        // PRODUCTION STEP: Bind device token immediately upon successful validation if provided
        if (signupOtpValidationRequest.getFcmToken() != null && !signupOtpValidationRequest.getFcmToken().isBlank()) {
            user.setFcmToken(signupOtpValidationRequest.getFcmToken());
        }

        userRepository.save(user);


        ProfilesEntity profilesEntity = new ProfilesEntity();
        profilesEntity.setUser(user);

        profileRepository.save(profilesEntity);

        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        matched.setOtpHash(null);
        matched.setOtpExpiry(null);
        matched.setHashAccessToken(jwtAccessToken);
        matched.setHashRefreshToken(jwtRefreshToken);
        matched.setRevoked(false);

        refreshTokenRepository.save(matched);

        return new LoginResponse(
                201,
                SuccessMsg.AUTH_201.name(),
                SuccessMsg.AUTH_201.getMessage(),
                request,
                jwtAccessToken,
                jwtRefreshToken
        );

    }

    // login service

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest, String path) throws CustomException {

        Optional<UserEntity> userEntity = userRepository.findByEmail(loginRequest.getEmail());

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_006,404);
        }

        if(userEntity.get().getAccountStatus() != AccountStatusEnum.VERIFIED){
            throw new CustomException(ErrorMsg.AUTH_003,400);
        }

        UserEntity user = userEntity.get();
        boolean result = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());

        if(!result){
            throw new CustomException(ErrorMsg.AUTH_007,401);
        }

        // PRODUCTION STEP: Update token during authentication if user switched phones or reinstalled the app
        if (loginRequest.getFcmToken() != null && !loginRequest.getFcmToken().isBlank()) {
            user.setFcmToken(loginRequest.getFcmToken());
            userRepository.save(user); // Flushes fresh token routing strings to database indices
        }

        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        // Persist hashed refresh token in dedicated table
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setUser(user);

        tokenEntity.setHashAccessToken(jwtAccessToken);
        tokenEntity.setHashRefreshToken(jwtRefreshToken);

        tokenEntity.setRevoked(false);
        refreshTokenRepository.save(tokenEntity);

        return new LoginResponse(
                200,
                SuccessMsg.USER_200.name(),
                SuccessMsg.USER_200.getMessage(),
                path,
                jwtAccessToken,
                jwtRefreshToken
        );

    }

    @Override
    @Transactional
    public RefreshTokenResponse newAccessToken(RefreshTokenRequest refreshTokenRequest, String path) throws CustomException {

        String providedToken = refreshTokenRequest.getRefreshToken();
        String userId = jwtService.extractUserId(providedToken);

        if (userId == null) {
            throw new CustomException(ErrorMsg.AUTH_006, 401);
        }

        UserEntity user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_006, 401));

        // find candidate refresh tokens for user and match by hash
        List<TokenEntity> tokens = refreshTokenRepository.findByUser(user);
        TokenEntity matched = null;

//        && t.getExpiresAt() != null && t.getExpiresAt().isAfter(Instant.now())

        for (TokenEntity t : tokens) {
            if (!t.isRevoked()) {
                if (providedToken.equals(t.getHashRefreshToken())) {
                    matched = t;
                    break;
                }
            }
        }

        if (matched == null) {
            throw new CustomException(ErrorMsg.AUTH_006, 401);
        }

        // rotate refresh token: revoke old, create new
        matched.setRevoked(true);
        refreshTokenRepository.save(matched);

//        String newRefreshToken = jwtService.generateRefreshToken(user.getId());
        String jwtAccessToken = jwtService.generateAccessToken(user.getId());

        TokenEntity newTokenEntity = new TokenEntity();
        newTokenEntity.setUser(user);

        newTokenEntity.setHashAccessToken(jwtAccessToken);
        newTokenEntity.setHashRefreshToken(refreshTokenRequest.getRefreshToken());
//        newTokenEntity.setExpiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        newTokenEntity.setRevoked(false);
        refreshTokenRepository.save(newTokenEntity);


        return new RefreshTokenResponse(
                200,
                SuccessMsg.GEN_200.name(),
                SuccessMsg.GEN_200.getMessage(),
                path,
                jwtAccessToken
        );

    }

    // email check up for reset password

    @Override
    @Transactional
    public AuthResponse forgetPasswordEmailRequest(
            ForgetPasswordEmailCheckupRequest forgetPasswordEmailCheckupRequest, String request) throws CustomException {

        Optional<UserEntity> userEntity = userRepository.findByEmail(forgetPasswordEmailCheckupRequest.getEmail());

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

        // store hashed otp in tokens table
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setUser(user);
        tokenEntity.setOtpHash(passwordEncoder.encode(String.valueOf(otp)));
        tokenEntity.setOtpExpiry(Instant.now().plusSeconds(expiration));
        tokenEntity.setRevoked(false);
        refreshTokenRepository.save(tokenEntity);

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
    public AuthResponse forgetPasswordOtpValidate(
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
        java.util.List<TokenEntity> tokens = refreshTokenRepository.findByUser(user);
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

        matched.setHashResetKey(resetKey);
        matched.setOtpHash(null);
        matched.setOtpExpiry(null);
        refreshTokenRepository.save(matched);

        return new AuthResponse(
                200,
                SuccessMsg.OTP_200.name(),
                SuccessMsg.OTP_200.getMessage(),
                request
        );

    }

    // password change logic

    @Override
    @Transactional
    public AuthResponse forgetPasswordRequest(
            ForgetPasswordRequest forgetPasswordRequest, String request) throws CustomException {

        //if reset password and confirm password does not match then throw mismatch Exception
        if(!forgetPasswordRequest.getResetPassword().equals(forgetPasswordRequest.getConfirmPassword())){
            throw new CustomException(ErrorMsg.AUTH_009,400);
        }

        // find token by reset key
        Optional<TokenEntity> tokenEntityOptional = refreshTokenRepository.findByHashResetKey(forgetPasswordRequest.getResetKey());
        if (tokenEntityOptional.isEmpty()) {
            throw new CustomException(ErrorMsg.AUTH_008,404);
        }

        TokenEntity tokenEntity = tokenEntityOptional.get();
        UserEntity user = tokenEntity.getUser();

        user.setPasswordHash(passwordEncoder.encode(forgetPasswordRequest.getResetPassword()));
        tokenEntity.setHashResetKey(null);
        refreshTokenRepository.save(tokenEntity);
        userRepository.save(user);

        return new AuthResponse(
                201,
                SuccessMsg.AUTH_205.name(),
                SuccessMsg.AUTH_205.getMessage(),
                request
        );
    }
}
