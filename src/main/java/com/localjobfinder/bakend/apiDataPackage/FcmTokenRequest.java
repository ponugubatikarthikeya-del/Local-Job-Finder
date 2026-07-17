package com.localjobfinder.bakend.apiDataPackage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenRequest {

    @NotBlank(message = "FCM Token cannot be blank")
    private String deviceFcmToken;

}
