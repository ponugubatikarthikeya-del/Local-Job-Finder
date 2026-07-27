package com.localjobfinder.bakend.apiDataPackage;

import com.localjobfinder.bakend.utilsPackage.ExceptionConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "username is required")
    @Size(min = 5 ,max = 50,message = ExceptionConstants.SIGN_USERNAME)
    private String username;

    @NotBlank(message = "phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = ExceptionConstants.SIGN_MOBILE)
    private String phone;

    @NotBlank(message = "email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = ExceptionConstants.SIGN_EMAIL)
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 8,message = ExceptionConstants.SIGN_PASSWORD)
    private String password;

//    @NotBlank(message = "device id is required")
//    private String deviceId;

}
