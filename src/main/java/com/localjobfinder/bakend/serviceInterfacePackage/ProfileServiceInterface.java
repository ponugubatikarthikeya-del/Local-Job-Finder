package com.localjobfinder.bakend.serviceInterfacePackage;

import com.localjobfinder.bakend.apiDataPackage.AddressTakeRequest;
import com.localjobfinder.bakend.apiDataPackage.EditProfileRequest;
import com.localjobfinder.bakend.dtoPackage.PublicProfileDto;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.AuthResponse;
import com.localjobfinder.bakend.responsePackage.MyProfileResponse;
import com.localjobfinder.bakend.responsePackage.ProfileResponse;

public interface ProfileServiceInterface {

    public MyProfileResponse getMyProfile(Long id, String request) throws CustomException;

    public MyProfileResponse getPublicProfile(Long id, String request) throws CustomException;

    public ProfileResponse updateProfile(Long id, String request, EditProfileRequest editProfileRequest) throws CustomException;

    public ProfileResponse updateUserAddress(Long id, AddressTakeRequest addressTakeRequest,String request) throws CustomException;
}
