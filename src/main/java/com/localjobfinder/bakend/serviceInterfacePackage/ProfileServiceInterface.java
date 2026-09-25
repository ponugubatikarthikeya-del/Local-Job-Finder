package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.apiDataPackage.AddressTakeRequest;
import com.localjobfinder.bakend.apiDataPackage.EditProfileRequest;
import com.localjobfinder.bakend.responsePackage.MyProfileResponse;
import com.localjobfinder.bakend.responsePackage.ProfileResponse;

public interface ProfileServiceInterface {

    public MyProfileResponse getMyProfile(Long id, String request) ;

    public MyProfileResponse getPublicProfile(Long id, String request) ;

    public ProfileResponse updateProfile(Long id, String request, EditProfileRequest editProfileRequest) ;

    public ProfileResponse updateUserAddress(Long id, AddressTakeRequest addressTakeRequest,String request);
}
