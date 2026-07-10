package com.localjobfinder.bakend.apiDataPackage;

import lombok.Data;

@Data
public class EditProfileRequest {
    // UserEntity updates
    private String username;
    private String phone;

    // ProfilesEntity updates
    private String headline;
    private String skills; // JSON String or structured object
    private String cityName;
    private String userDescription;
}
