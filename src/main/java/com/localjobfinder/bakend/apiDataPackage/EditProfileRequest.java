package com.localjobfinder.bakend.apiDataPackage;

import lombok.Data;

import java.util.List;

@Data
public class EditProfileRequest {
    // UserEntity updates
    private String username;
    private String phone;

    // ProfilesEntity updates
    private String headline;
    private String dateOfBirth;
    private List<String> skills; // JSON String or structured object
    private String cityName;
    private String userDescription;
}
