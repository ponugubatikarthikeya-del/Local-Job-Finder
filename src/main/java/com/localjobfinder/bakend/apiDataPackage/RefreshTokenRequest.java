package com.localjobfinder.bakend.apiDataPackage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "refresh token is required")
    private String refreshToken;

}
