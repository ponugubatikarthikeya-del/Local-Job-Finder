package com.localjobfinder.bakend.apiDataPackage;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SignupOtpValidationRequest {

    @NotBlank(message = "email is invalid")
    @Email(message = "invalid email")
    private String email;

    @NotNull(message = "otp is invalid")
    @Positive(message = "otp must be positive")
    private Integer otp;

    @NotBlank(message = "fcm token is required")
    private String fcmToken;
}
