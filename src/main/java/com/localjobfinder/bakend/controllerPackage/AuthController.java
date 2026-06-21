package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.responsePackage.ApiResponse;
import com.localjobfinder.bakend.responsePackage.LoginResponse;
import com.localjobfinder.bakend.servicePackage.AuthService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/")
public class AuthController {

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
    public ResponseEntity<ApiResponse> signup(
            @Valid @RequestBody SignupRequest signupRequest,
            HttpServletRequest httpServletRequest) throws CustomException {

        System.out.println("signup api is calling");

        ApiResponse response = authService.signup(signupRequest, getPath(httpServletRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    //login api
    @PostMapping("login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpServletRequest) throws CustomException {

        System.out.println("login api is calling");

        LoginResponse response = authService.login(loginRequest, getPath(httpServletRequest));

        HttpHeaders headers = new HttpHeaders();
         headers.add(HttpHeaders.AUTHORIZATION,"Bearer " + response.getAccessToken());

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).headers(headers).body(response);

    }

    // refresh token generate api
    @PostMapping("refreshToken")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest,
            HttpServletRequest httpServletRequest) throws CustomException {
        System.out.println("refreshToken api is calling");

         LoginResponse response = authService.newAccessToken(refreshTokenRequest,getPath(httpServletRequest));

         return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    @GetMapping("getUserDetails/{id}")
    public ResponseEntity<UserResponse> getUserDetails(
            @Valid @PathVariable("id") Long id, HttpServletRequest httpServletRequest) throws CustomException {

        System.out.println("getUserDetails api is calling");

        UserResponse response = authService.getUserDetails(id, getPath(httpServletRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    // forget password email checkup

    @PostMapping("forgotPwEmailRequest")
    public ResponseEntity<ApiResponse> forgotEmailRequest(
            @Valid @RequestBody ForgetPasswordEmailCheckupRequest forgetPasswordEmailCheckupRequest,
            HttpServletRequest httpRequest) throws CustomException{

        System.out.println("Forgot Email API called");

        ApiResponse response = authService.forgetPasswordEmailRequest(forgetPasswordEmailCheckupRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    // otp validate api

    @PostMapping("resetPasswordOtp")
    public ResponseEntity<ApiResponse> forgotPasswordOtpValidate(
            @Valid @RequestBody ForgetPasswordOtpValidateRequest forgetPasswordOtpValidateRequest,
            HttpServletRequest httpRequest) throws CustomException {
        System.out.println("Forgot otp validate API called");

        ApiResponse response = authService.forgetPasswordOtpValidate(forgetPasswordOtpValidateRequest,getPath(httpRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    // change password api

    @PostMapping("resetPasswordRequest")
    public ResponseEntity<ApiResponse> forgetPasswordRequest
            (@Valid @RequestBody ForgetPasswordRequest forgetPasswordRequest, HttpServletRequest httpRequest)
           throws CustomException {
        System.out.println("Reset Password API called");

        ApiResponse response = authService.forgetPasswordRequest(forgetPasswordRequest,getPath(httpRequest));
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

