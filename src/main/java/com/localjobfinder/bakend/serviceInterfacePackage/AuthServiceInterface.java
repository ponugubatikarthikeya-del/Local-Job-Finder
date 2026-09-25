package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.OauthPackage.OAuthLoginRequest;
import com.localjobfinder.bakend.apiDataPackage.*;
import com.localjobfinder.bakend.responsePackage.*;


public interface AuthServiceInterface {

    public AuthResponse signup(SignupRequest signupRequest, String request,String deviceId)
            throws Exception;

    public LoginResponse signupOtpVerification(SignupOtpValidationRequest signupOtpValidationRequest, String request,String deviceId);

    public AuthResponse resendOtp(EmailCheckupRequest emailCheckupRequest, String request,String deviceId) throws Exception;

    public LoginResponse login(LoginRequest loginRequest, String request,String deviceId)
            throws Exception;

    public RefreshTokenResponse newAccessToken(String token, String request);

    public AuthResponse forgetPasswordEmailRequest(
            EmailCheckupRequest emailCheckupRequest, String request) throws  Exception;

    public ResetKeyOtpResponse forgetPasswordOtpValidate(
            ResetPasswordOtpValidateRequest resetPasswordOtpValidateRequest, String request) ;

    public AuthResponse forgetPasswordRequest(
            PasswordRequest passwordRequest, String request);

    public LoginResponse socialLogin (OAuthLoginRequest oAuthLoginRequest, String request,String deviceId) ;

    public AuthResponse logout(Long userId,String DeviceId,boolean allDevices,String request,FcmTokenRequest fcmTokenRequest);
}
