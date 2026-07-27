package com.localjobfinder.bakend.responsePackage;

import lombok.Getter;

import java.time.Instant;

@Getter
public class RefreshTokenResponse {

    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final String accessToken;
    private final String refreshToken;

    public RefreshTokenResponse(int status, String errorCode,
                         String message, String path, String accessToken, String refreshToken) {
        this.timestamp = Instant.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
