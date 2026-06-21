package com.localjobfinder.bakend.apiDataPackage;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForgetPasswordOtpValidateRequest {

    @NotNull(message = "otp is required")
    private int otp;

}
