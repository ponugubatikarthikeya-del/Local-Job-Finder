package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.responsePackage.AuthResponse;

public interface FcmServiceInterface {

    public AuthResponse updateFcmToken(Long currentUserId, String deviceFcmToken,String path);

//    public AuthResponse removeTokenOnLogout(Long currentUserId, String deviceFcmToken, String path) throws CustomException;
}
