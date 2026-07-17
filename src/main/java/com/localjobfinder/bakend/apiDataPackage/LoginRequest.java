package com.localjobfinder.bakend.apiDataPackage;

import com.localjobfinder.bakend.utilsPackage.ExceptionConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = ExceptionConstants.SIGN_EMAIL)
    private String email;

    @Size(min = 8, message = ExceptionConstants.LOGIN_PASSWORD)
    private String password;

}
