package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.OauthPackage.OAuthLoginRequest;
import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.*;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtSecurityService;
import com.localjobfinder.bakend.servicePackage.AuthService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("user/auth/")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private JwtSecurityService jwtSecurityService;

    @Autowired
    public AuthController(AuthService authService, JwtSecurityService jwtSecurityService) {
        this.authService = authService;
        this.jwtSecurityService = jwtSecurityService;
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

        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        AuthResponse response = authService.signup(signupRequest, getPath(httpServletRequest),deviceId);

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    @PostMapping("signupOtp-verification")
    public ResponseEntity<LoginResponse> signupOtpVerification(
            @Valid @RequestBody SignupOtpValidationRequest signupOtpValidationRequest,
            HttpServletRequest httpServletRequest
    )  throws CustomException {

        log.info("signup otp api called for email={}", signupOtpValidationRequest.getEmail());

        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        LoginResponse response = authService.signupOtpVerification(signupOtpValidationRequest,getPath(httpServletRequest),deviceId);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    @PostMapping("resend-otp")
    public ResponseEntity<AuthResponse> resendOtpVerification(
            @Valid @RequestBody EmailCheckupRequest emailCheckupRequest,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

        log.info("resend otp api called for email={}", emailCheckupRequest.getEmail());

        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        AuthResponse response = authService.resendOtp(emailCheckupRequest,getPath(httpServletRequest),deviceId);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    //login api
    @PostMapping("login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest) throws CustomException {

        log.info("login api called for email={}", loginRequest.getEmail());

        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        LoginResponse response = authService.login(loginRequest, getPath(httpServletRequest),deviceId);

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    // refresh token generate api
    @PostMapping("refresh/token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @RequestHeader("Authorization") String authHeader, // 1. Extracts the raw header string
            HttpServletRequest httpServletRequest) throws CustomException {
        log.info("refreshToken api called");

        // 2. Safely extract the token by removing the "Bearer " prefix
        String refreshToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            refreshToken = authHeader.substring(7);
        } else {
            throw new CustomException(ErrorMsg.AUTH_010, 400);
        }

        Long userId = jwtSecurityService.getCurrentUserId();

         RefreshTokenResponse response = authService.newAccessToken(userId,refreshToken,getPath(httpServletRequest));

         return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    // forget password email checkup

    @PatchMapping("forgot-password")
    public ResponseEntity<AuthResponse> forgotEmailRequest(
            @Valid @RequestBody EmailCheckupRequest emailCheckupRequest,
            HttpServletRequest httpRequest) throws CustomException{

        log.info("forgot password email request for email={}", emailCheckupRequest.getEmail());

        AuthResponse response = authService.forgetPasswordEmailRequest(emailCheckupRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    // otp validate api

    @PatchMapping("reset-otp-verify")
    public ResponseEntity<ResetKeyOtpResponse> forgotPasswordOtpValidate(
            @Valid @RequestBody ResetPasswordOtpValidateRequest resetPasswordOtpValidateRequest,
            HttpServletRequest httpRequest) throws CustomException {
        log.info("forgot password otp validate called for otp={}", resetPasswordOtpValidateRequest.getOtp());

        ResetKeyOtpResponse response = authService.forgetPasswordOtpValidate(resetPasswordOtpValidateRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    // change password api

    @PatchMapping("change-password")
    public ResponseEntity<AuthResponse> forgetPasswordRequest
            (@Valid @RequestBody PasswordRequest passwordRequest, HttpServletRequest httpRequest)
           throws CustomException {
        log.info("reset password request called for resetKey={}", passwordRequest.getResetKey());

        AuthResponse response = authService.forgetPasswordRequest(passwordRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    @PostMapping("social-login")
    public ResponseEntity<LoginResponse> socialLogin(
            @Valid @RequestBody OAuthLoginRequest oAuthLoginRequest,
            HttpServletRequest httpServletRequest) throws CustomException {

        log.info("Social Login initiated for provider={}", oAuthLoginRequest.getProvider());

        String deviceId = httpServletRequest.getHeader("X-Device-Id");

        LoginResponse response = authService.socialLogin(oAuthLoginRequest, getPath(httpServletRequest),deviceId);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    @GetMapping("getData")
    public String getData(){
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://jsonplaceholder.typicode.com/posts/1";

        log.info("external api called for url={}", url);

        String response = restTemplate.getForObject(url, String.class);

        return response;

    }
}

