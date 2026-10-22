package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.InvalidException;
import com.localjobfinder.bakend.responsePackage.*;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtDataProviderService;
import com.localjobfinder.bakend.serviceInterfacePackage.AuthServiceInterface;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("user/auth/")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthServiceInterface authService;
    private final JwtDataProviderService jwtDataProviderService;

    @Autowired
    public AuthController(AuthServiceInterface authService, JwtDataProviderService jwtDataProviderService) {
        this.authService = authService;
        this.jwtDataProviderService = jwtDataProviderService;
    }

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    //signup api
    @PostMapping("signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest signupRequest,
            HttpServletRequest httpServletRequest) throws Exception {

        log.info("signup api called for email={}", signupRequest.getEmail());

        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        AuthResponse response = authService.signup(signupRequest, getPath(httpServletRequest),deviceId);

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    @PostMapping("signupOtp-verification")
    public ResponseEntity<LoginResponse<Map<String,String>>> signupOtpVerification(
            @Valid @RequestBody SignupOtpValidationRequest signupOtpValidationRequest,
            HttpServletRequest httpServletRequest
    ) {
        log.info("signup otp api called for email={}", signupOtpValidationRequest.getEmail());

        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        LoginResponse<Map<String,String>> response = authService.signupOtpVerification(signupOtpValidationRequest,getPath(httpServletRequest),deviceId);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    @PostMapping("resend-otp")
    public ResponseEntity<AuthResponse> resendOtpVerification(
            @Valid @RequestBody EmailCheckupRequest emailCheckupRequest,
            HttpServletRequest httpServletRequest
    ) throws Exception {

        log.info("resend otp api called for email={}", emailCheckupRequest.getEmail());

        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        AuthResponse response = authService.resendOtp(emailCheckupRequest,getPath(httpServletRequest),deviceId);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    //login api
    @PostMapping("login")
    public ResponseEntity<LoginResponse<Map<String,String>>> login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest) throws Exception {

        log.info("login api called for email={}", loginRequest.getEmail());

        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        LoginResponse<Map<String,String>> response = authService.login(loginRequest, getPath(httpServletRequest),deviceId);

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    // refresh token generate api

    @PostMapping("refresh/token")
    public ResponseEntity<LoginResponse<Map<String,String>>> refreshToken(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            HttpServletRequest httpServletRequest) {

        log.info("refreshToken API called");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidException(ErrorMsg.AUTH_010.getMessage());
        }

        String refreshToken = authHeader.substring(7);

        LoginResponse<Map<String,String>> response = authService.newAccessToken(refreshToken, getPath(httpServletRequest));

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    // forget password email checkup

    @PatchMapping("forgot-password")
    public ResponseEntity<AuthResponse> forgotEmailRequest(
            @Valid @RequestBody EmailCheckupRequest emailCheckupRequest,
            HttpServletRequest httpRequest) throws Exception {

        log.info("forgot password email request for email={}", emailCheckupRequest.getEmail());

        AuthResponse response = authService.forgetPasswordEmailRequest(emailCheckupRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    // otp validate api

    @PatchMapping("reset-otp-verify")
    public ResponseEntity<ResetKeyOtpResponse> forgotPasswordOtpValidate(
            @Valid @RequestBody ResetPasswordOtpValidateRequest resetPasswordOtpValidateRequest,
            HttpServletRequest httpRequest) {
        log.info("forgot password otp validate called for otp={}", resetPasswordOtpValidateRequest.getOtp());

        ResetKeyOtpResponse response = authService.forgetPasswordOtpValidate(resetPasswordOtpValidateRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    // change password api

    @PatchMapping("change-password")
    public ResponseEntity<AuthResponse> forgetPasswordRequest
            (@Valid @RequestBody PasswordRequest passwordRequest, HttpServletRequest httpRequest) {
        log.info("reset password request called for resetKey={}", passwordRequest.getResetKey());

        AuthResponse response = authService.forgetPasswordRequest(passwordRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

//    @PostMapping("social-login")
//    public ResponseEntity<LoginResponse<Map<String,String>>> socialLogin(
//            @Valid @RequestBody OAuthLoginRequest oAuthLoginRequest,
//            HttpServletRequest httpServletRequest) {
//
//        log.info("Social Login initiated for provider={}", oAuthLoginRequest.getProvider());
//
//        String deviceId = httpServletRequest.getHeader("X-Device-Id");
//
//        LoginResponse<Map<String,String>> response = authService.socialLogin(oAuthLoginRequest, getPath(httpServletRequest),deviceId);
//        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
//    }

    @PostMapping("logout")
    public ResponseEntity<AuthResponse> logout(
            @RequestHeader(value = "X-Device-Id", defaultValue = "DEFAULT_DEVICE",required = false) String deviceId,
            @RequestParam(value = "allDevices", defaultValue = "false") boolean allDevices,
            @Valid @RequestBody(required = false) FcmTokenRequest fcmTokenRequest,
            HttpServletRequest httpServletRequest) {

        log.info("logout api called for deviceId={}", deviceId);

        Long userId = jwtDataProviderService.getCurrentUserId();

        AuthResponse response = authService.logout(userId,deviceId,allDevices,getPath(httpServletRequest),fcmTokenRequest);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

}

