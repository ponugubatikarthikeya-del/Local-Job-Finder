package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.*;


public interface AuthServiceInterface {

    public AuthResponse signup(SignupRequest signupRequest, String request)
            throws CustomException;

    public LoginResponse signupOtpVerification(SignupOtpValidationRequest signupOtpValidationRequest, String request)
            throws CustomException;

    public LoginResponse login(LoginRequest loginRequest, String request)
            throws CustomException;

    public RefreshTokenResponse newAccessToken(RefreshTokenRequest refreshTokenRequest, String request)
         throws CustomException;

    public AuthResponse forgetPasswordEmailRequest(
            ForgetPasswordEmailCheckupRequest forgetPasswordEmailCheckupRequest, String request) throws CustomException;

    public AuthResponse forgetPasswordOtpValidate(
            ResetPasswordOtpValidateRequest resetPasswordOtpValidateRequest, String request) throws CustomException;

    public AuthResponse forgetPasswordRequest(
            ForgetPasswordRequest forgetPasswordRequest, String request) throws CustomException;
}
