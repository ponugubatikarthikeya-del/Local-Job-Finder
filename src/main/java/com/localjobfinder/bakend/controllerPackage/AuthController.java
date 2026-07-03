package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.RefreshTokenResponse;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
import com.localjobfinder.bakend.responsePackage.LoginResponse;
import com.localjobfinder.bakend.servicePackage.AuthService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    //signup api
    @PostMapping("signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest signupRequest,
            HttpServletRequest httpServletRequest) throws CustomException {

        log.info("signup api called for email={}", signupRequest.getEmail());

        AuthResponse response = authService.signup(signupRequest, getPath(httpServletRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    //login api
    @PostMapping("login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest) throws CustomException {

        log.info("login api called for email={}", loginRequest.getEmail());

        LoginResponse response = authService.login(loginRequest, getPath(httpServletRequest));

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    // refresh token generate api
    @PostMapping("refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest httpServletRequest) throws CustomException {
        log.info("refreshToken api called");

         RefreshTokenResponse response = authService.newAccessToken(refreshTokenRequest,getPath(httpServletRequest));

         return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    @GetMapping("getUserDetails")
    public ResponseEntity<UserResponse> getUserDetails(
            @Valid Authentication authentication, HttpServletRequest httpServletRequest) throws CustomException {

                // get userId for jwt token
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorMsg.AUTH_004, 401);
        }

        String userId = (String) authentication.getPrincipal();

        log.info("getUserDetails api called for id={}",userId );

        UserResponse response = authService.getUserDetails(userId, getPath(httpServletRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    // forget password email checkup

    @PostMapping("forgot-password")
    public ResponseEntity<AuthResponse> forgotEmailRequest(
            @Valid @RequestBody ForgetPasswordEmailCheckupRequest forgetPasswordEmailCheckupRequest,
            HttpServletRequest httpRequest) throws CustomException{

        log.info("forgot password email request for email={}", forgetPasswordEmailCheckupRequest.getEmail());

        AuthResponse response = authService.forgetPasswordEmailRequest(forgetPasswordEmailCheckupRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    // otp validate api

    @PostMapping("verify-otp")
    public ResponseEntity<AuthResponse> forgotPasswordOtpValidate(
            @Valid @RequestBody ForgetPasswordOtpValidateRequest forgetPasswordOtpValidateRequest,
            HttpServletRequest httpRequest) throws CustomException {
        log.info("forgot password otp validate called for otp={}", forgetPasswordOtpValidateRequest.getOtp());

        AuthResponse response = authService.forgetPasswordOtpValidate(forgetPasswordOtpValidateRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    // change password api

    @PostMapping("change-password")
    public ResponseEntity<AuthResponse> forgetPasswordRequest
            (@Valid @RequestBody ForgetPasswordRequest forgetPasswordRequest, HttpServletRequest httpRequest)
           throws CustomException {
        log.info("reset password request called for resetKey={}", forgetPasswordRequest.getResetKey());

        AuthResponse response = authService.forgetPasswordRequest(forgetPasswordRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    @GetMapping("sample")
    public ResponseEntity<UserResponse> sample(HttpServletRequest httpRequest) throws CustomException {

        String username = "hey karthikeya you are very handsome";

        UserResponse userResponse = new UserResponse(
                200,
                SuccessMsg.GEN_200.name(),
                SuccessMsg.GEN_200.getMessage(),
                getPath(httpRequest),
                username
        );

        return ResponseEntity.status(HttpStatusCode.valueOf(userResponse.getStatus())).body(userResponse);
        }

}

