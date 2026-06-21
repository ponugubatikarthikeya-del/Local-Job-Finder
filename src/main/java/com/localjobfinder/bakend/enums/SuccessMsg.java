package com.localjobfinder.bakend.enums;


import lombok.Getter;

@Getter
public enum SuccessMsg {
    // 🔐 AUTH SUCCESS
    AUTH_200("Login successful."),
    AUTH_201("Registration successful."),
    AUTH_202("Token refreshed successfully."),
    AUTH_203("Logout successful."),
    AUTH_204("Email Send Successfully."),
    AUTH_205("Password reset successful."),

    // 👤 USER / PROFILE
    USER_200("User fetched successfully."),
    USER_201("User created successfully."),
    USER_202("User updated successfully."),
    USER_203("User deleted successfully."),
    PROFILE_200("Profile fetched successfully."),
    PROFILE_201("Profile updated successfully."),

    // 💼 JOB
    JOB_200("Job fetched successfully."),
    JOB_201("Job created successfully."),
    JOB_202("Job updated successfully."),
    JOB_203("Job deleted successfully."),
    JOB_204("Job status updated successfully."),

    // 📄 APPLICATION
    APP_200("Application submitted successfully."),
    APP_201("Application fetched successfully."),
    APP_202("Application status updated successfully."),
    APP_203("Application withdrawn successfully."),

    // 📂 FILE
    FILE_200("File uploaded successfully."),
    FILE_201("File fetched successfully."),
    FILE_202("File deleted successfully."),

    // 🔔 GENERAL
    GEN_200("Request successful."),


    OTP_200("OTP verified successfully.");
    private final String message;

    SuccessMsg(String message) {
        this.message = message;
    }

}
