package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.responsePackage.ApiResponse;
import com.localjobfinder.bakend.responsePackage.LoginResponse;


public interface AuthServiceInterface {

    public ApiResponse signup(SignupRequest signupRequest, String request)
            throws CustomException;

    public LoginResponse login(LoginRequest loginRequest, String request)
            throws CustomException;

    public LoginResponse newAccessToken(RefreshTokenRequest refreshTokenRequest, String request)
         throws CustomException;

    public UserResponse getUserDetails(Long id, String request) throws CustomException;

    public ApiResponse forgetPasswordEmailRequest(
            ForgetPasswordEmailCheckupRequest forgetPasswordEmailCheckupRequest, String request) throws CustomException;

    public ApiResponse forgetPasswordOtpValidate(
            ForgetPasswordOtpValidateRequest forgetPasswordOtpValidateRequest, String request) throws CustomException;

    public ApiResponse forgetPasswordRequest(
            ForgetPasswordRequest forgetPasswordRequest, String request) throws CustomException;
}
