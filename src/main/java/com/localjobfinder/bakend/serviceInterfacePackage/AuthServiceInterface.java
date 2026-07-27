package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.OauthPackage.OAuthLoginRequest;
import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.*;


public interface AuthServiceInterface {

    public AuthResponse signup(SignupRequest signupRequest, String request,String deviceId)
            throws CustomException;

    public LoginResponse signupOtpVerification(SignupOtpValidationRequest signupOtpValidationRequest, String request,String deviceId)
            throws CustomException;

    public AuthResponse resendOtp(EmailCheckupRequest emailCheckupRequest, String request,String deviceId) throws CustomException;

    public LoginResponse login(LoginRequest loginRequest, String request,String deviceId)
            throws CustomException;

    public RefreshTokenResponse newAccessToken(Long userId,String token, String request)
         throws CustomException;

    public AuthResponse forgetPasswordEmailRequest(
            EmailCheckupRequest emailCheckupRequest, String request) throws CustomException;

    public ResetKeyOtpResponse forgetPasswordOtpValidate(
            ResetPasswordOtpValidateRequest resetPasswordOtpValidateRequest, String request) throws CustomException;

    public AuthResponse forgetPasswordRequest(
            PasswordRequest passwordRequest, String request) throws CustomException;

    public LoginResponse socialLogin (OAuthLoginRequest oAuthLoginRequest, String request,String deviceId) throws CustomException;
}
