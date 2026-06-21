package com.localjobfinder.bakend.apiDataPackage;

import com.localjobfinder.bakend.utilsPackage.ExceptionConstants;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @Size(min = 5 ,max = 50,message = ExceptionConstants.SIGN_USERNAME)
    private String username;

    @Pattern(regexp = "^[0-9]{10}$", message = ExceptionConstants.SIGN_MOBILE)
    private String phone;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = ExceptionConstants.SIGN_EMAIL)
    private String email;

    @Size(min = 6,message = ExceptionConstants.SIGN_PASSWORD)
    private String password;

}
