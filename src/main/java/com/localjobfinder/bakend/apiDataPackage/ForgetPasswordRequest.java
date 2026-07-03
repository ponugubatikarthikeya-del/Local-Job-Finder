package com.localjobfinder.bakend.apiDataPackage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgetPasswordRequest {

    @NotNull(message = "reset key required")
    private String resetKey ;

    @NotNull(message = "password is required")
    @Size(min = 8)
    private String resetPassword;

    @NotNull(message = "password is required")
    private String confirmPassword;
}
