package com.localjobfinder.bakend.OauthPackage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUserDto {
    private String providerId; // Unique ID from the provider (e.g., Google subject or GitHub ID)
    private String email;
    private String name;
    private AuthLoginEnum providerName; // "GOOGLE", "GITHUB", "FACEBOOK"
    private boolean isEmailVerified;
}
