package com.localjobfinder.bakend.OauthPackage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthLoginRequest {

    @NotBlank(message = "OAuth Token or Code is required")
    private String token; // Can be an ID Token or Access Token depending on provider

    @NotBlank(message = "Provider name is required (e.g., google, github, facebook)")
    private String provider;

//    @NotBlank(message = "device id is required")
//    private String deviceId;

}