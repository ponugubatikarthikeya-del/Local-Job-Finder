package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.RefreshTokenResponse;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
import com.localjobfinder.bakend.responsePackage.LoginResponse;


public interface AuthServiceInterface {

    public AuthResponse signup(SignupRequest signupRequest, String request)
            throws CustomException;

    public LoginResponse login(LoginRequest loginRequest, String request)
            throws CustomException;

    public RefreshTokenResponse newAccessToken(RefreshTokenRequest refreshTokenRequest, String request)
         throws CustomException;

    public UserResponse getUserDetails(String id, String request) throws CustomException;

    public AuthResponse forgetPasswordEmailRequest(
            ForgetPasswordEmailCheckupRequest forgetPasswordEmailCheckupRequest, String request) throws CustomException;

    public AuthResponse forgetPasswordOtpValidate(
            ForgetPasswordOtpValidateRequest forgetPasswordOtpValidateRequest, String request) throws CustomException;

    public AuthResponse forgetPasswordRequest(
            ForgetPasswordRequest forgetPasswordRequest, String request) throws CustomException;
}
