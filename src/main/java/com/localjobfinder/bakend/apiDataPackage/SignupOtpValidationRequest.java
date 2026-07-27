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

    @NotBlank(message = "otp is invalid")
    private String otp;
//
//    @NotBlank(message = "device id is required")
//    private String deviceId;

}
