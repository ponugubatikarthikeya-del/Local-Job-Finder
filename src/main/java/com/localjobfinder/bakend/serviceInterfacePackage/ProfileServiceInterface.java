package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.apiDataPackage.AddressTakeRequest;
import com.localjobfinder.bakend.apiDataPackage.EditProfileRequest;
import com.localjobfinder.bakend.dtoPackage.PublicProfileDto;
import com.localjobfinder.bakend.dtoPackage.UserDto;
import com.localjobfinder.bakend.responsePackage.UserProfileResponse;
import com.localjobfinder.bakend.responsePackage.ProfileResponse;

public interface ProfileServiceInterface {

    public UserProfileResponse<UserDto> getMyProfile(Long id, String request) ;

    public UserProfileResponse<PublicProfileDto> getPublicProfile(Long id, String request) ;

    public ProfileResponse updateProfile(Long id, String request, EditProfileRequest editProfileRequest) ;

    public ProfileResponse updateUserAddress(Long id, AddressTakeRequest addressTakeRequest,String request);
}
