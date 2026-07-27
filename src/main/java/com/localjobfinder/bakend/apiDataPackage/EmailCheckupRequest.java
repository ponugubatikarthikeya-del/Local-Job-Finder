package com.localjobfinder.bakend.apiDataPackage;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmailCheckupRequest {

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = "Invalid email")
    private String email;

}
