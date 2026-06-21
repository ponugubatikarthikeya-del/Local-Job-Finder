package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.converterPackage.UserConverter;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.dbEntity.WorkerProfileEntity;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.jwtTokenPackage.JwtService;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.repository.WorkerProfileRepository;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.responsePackage.ApiResponse;
import com.localjobfinder.bakend.responsePackage.LoginResponse;
import com.localjobfinder.bakend.sendMailPackage.ForgetEmailSend;
import com.localjobfinder.bakend.serviceInterfacePackage.AuthServiceInterface;
import com.localjobfinder.bakend.utilsPackage.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService implements AuthServiceInterface {

    private UserRepository userRepository;
    private JwtService jwtService;
    private ForgetEmailSend forgetEmailSend;
    private PasswordEncoder passwordEncoder;
    private WorkerProfileRepository workerProfileRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository
                                             , JwtService jwtService,
                                  ForgetEmailSend forgetEmailSend,
                                  PasswordEncoder passwordEncoder, WorkerProfileRepository workerProfileRepository)
    {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.forgetEmailSend = forgetEmailSend;
        this .passwordEncoder = passwordEncoder;
        this.workerProfileRepository = workerProfileRepository;
    }

    @Override
    public ApiResponse signup(SignupRequest signupRequest, String path) throws CustomException{

        //take user details and check if user email already exist or not
        Optional<UserEntity> userEntity = userRepository.findByEmail(signupRequest.getEmail());

        if(userEntity.isPresent()){
            throw new CustomException(ErrorMsg.AUTH_005,400);
        }

        UserEntity user = new UserEntity();

        user.setName(signupRequest.getUsername());
        user.setPhone(signupRequest.getPhone());
        user.setEmail(signupRequest.getEmail());

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        user.setPasswordHash(encodedPassword);

        userRepository.save(user);

        WorkerProfileEntity  workerProfileEntity = new WorkerProfileEntity();
        workerProfileEntity.setUser(user);

        workerProfileRepository.save(workerProfileEntity);

       return new ApiResponse(
               201,
               SuccessMsg.AUTH_201.name(),
               SuccessMsg.AUTH_201.getMessage(),
               path
       );
    }

    // login service


    @Override
    public LoginResponse login(LoginRequest loginRequest, String path) throws CustomException {

        Optional<UserEntity> userEntity = userRepository.findByEmail(loginRequest.getEmail());

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_006,401);
        }

        UserEntity user = userEntity.get();
        boolean result = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());

        if(!result){
            throw new CustomException(ErrorMsg.AUTH_007,401);
        }

//        return new ApiDataResponse(
//                200,
//                SuccessMsg.AUTH_203.name(),
//                SuccessMsg.AUTH_200.getMessage(),
//                path,
//                user
//        );

        String jwtAccessToken = jwtService.generateAccessToken(user.getId());
        String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

        user.setAccessToken(jwtAccessToken);
        user.setRefreshToken(jwtRefreshToken);
        userRepository.save(user);

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
    public LoginResponse newAccessToken(RefreshTokenRequest refreshTokenRequest, String path) throws CustomException {

        Optional<UserEntity> userEntity = userRepository.findByRefreshToken(refreshTokenRequest.getRefreshToken());

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_006,401);
        }

        UserEntity user = userEntity.get();

        String userId = jwtService.extractUserId(refreshTokenRequest.getRefreshToken());

        String jwtAccessToken = jwtService.generateAccessToken(Long.parseLong(userId));
        String jwtRefreshToken = user.getRefreshToken();

        user.setAccessToken(jwtAccessToken);
        userRepository.save(user);

        System.out.println("jwtAccessToken: " + jwtAccessToken);

        return new LoginResponse(
                200,
                SuccessMsg.GEN_200.name(),
                SuccessMsg.GEN_200.getMessage(),
                path,
                jwtAccessToken,
                jwtRefreshToken
        );

    }

    // get user details by path variable user id

    public UserResponse getUserDetails(Long id, String path) throws CustomException{
        Optional<UserEntity> userEntity = userRepository.findById(id);

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_008,404);
        }

        UserEntity user = userEntity.get();

        UserConverter userConverter = new UserConverter();

       userConverter.setName(user.getName());
        userConverter.setPhone(user.getPhone());
        userConverter.setEmail(user.getEmail());

        return new UserResponse(
                200,
                SuccessMsg.USER_200.name(),
                SuccessMsg.USER_200.getMessage(),
                path,
               userConverter
        );
    }


    // email check up for reset password


    @Override
    public ApiResponse forgetPasswordEmailRequest(
            ForgetPasswordEmailCheckupRequest forgetPasswordEmailCheckupRequest, String request) throws CustomException {

        Optional<UserEntity> userEntity = userRepository.findByEmail(forgetPasswordEmailCheckupRequest.getEmail());

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_008,404);
        }
        UserEntity user = userEntity.get();

        int otp = RandomUtil.generateOtp();

        long expiration = 300 ;

        String text = "Hi " + user.getName() + ",\n\n"
                + "Here is your reset password OTP: " + otp + "\n"
                + "This OTP will expire in " + expiration + " seconds.\n\n"
                + "If you did not request a password reset, please ignore this email.";

        forgetEmailSend.mailSender(user.getEmail(),otp,text);

        user.setOtp(otp);
        user.setOtpExpiry(Instant.now().plusSeconds(expiration));
        userRepository.save(user);

//        // when validating
//        if(Instant.now().isAfter(user.getOtpExpiry())){
//            throw new CustomException(ErrorMsg.OTP_001,400);
//        }

        return new ApiResponse(
                200,
                SuccessMsg.AUTH_204.name(),
                SuccessMsg.AUTH_204.getMessage(),
                request
        );
    }

    // reset password otp validation


    @Override
    public ApiResponse forgetPasswordOtpValidate(
            ForgetPasswordOtpValidateRequest forgetPasswordOtpValidateRequest, String request) throws CustomException {

        if(forgetPasswordOtpValidateRequest.getOtp() <= 0){
            throw new CustomException(ErrorMsg.OTP_002,401);
        }

        Optional<UserEntity> userEntity = userRepository.findByOtp(forgetPasswordOtpValidateRequest.getOtp());

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.OTP_002,404);
        }

        UserEntity user = userEntity.get();

        // check otp expiration
        if(Instant.now().isAfter(user.getOtpExpiry())){
            throw new CustomException(ErrorMsg.OTP_001,400);
        }

        //check otp match
        if(user.getOtp() != forgetPasswordOtpValidateRequest.getOtp()){
            throw new CustomException(ErrorMsg.OTP_002,400);
        }

        String resetKey = RandomUtil.randomString();
        System.out.println("resetKey = " + resetKey);

        user.setResetKey(resetKey);
        user.setOtp(0);
        userRepository.save(user);

        return new ApiResponse(
                200,
                SuccessMsg.OTP_200.name(),
                SuccessMsg.OTP_200.getMessage(),
                request
        );

    }

    // password change logic

    @Override
    public ApiResponse forgetPasswordRequest(
            ForgetPasswordRequest forgetPasswordRequest, String request) throws CustomException {

        //if reset password and confirm password does not then throw mismatch Exception
        if(!forgetPasswordRequest.getResetPassword().equals(forgetPasswordRequest.getConfirmPassword())){
            throw new CustomException(ErrorMsg.AUTH_009,400);
        }

        Optional<UserEntity> userEntity = userRepository.findByResetKey(forgetPasswordRequest.getResetKey());

        if(userEntity.isEmpty()){
            throw new CustomException(ErrorMsg.AUTH_008,404);
        }

        UserEntity user = userEntity.get();

        user.setPasswordHash(passwordEncoder.encode(forgetPasswordRequest.getResetPassword()));
        user.setResetKey(null);
        userRepository.save(user);

        return new ApiResponse(
                201,
                SuccessMsg.AUTH_205.name(),
                SuccessMsg.AUTH_205.getMessage(),
                request
        );
    }
}
