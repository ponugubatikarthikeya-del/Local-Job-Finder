package com.localjobfinder.bakend.apiDataPackage;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgetPasswordOtpValidateRequest {

    @NotBlank(message = "email is required")
    @Email(message = "invalid email")
    private String email;

    @NotNull(message = "otp is required")
    private Integer otp;

}
