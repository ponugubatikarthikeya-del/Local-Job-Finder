package com.localjobfinder.bakend.responsePackage;

import lombok.Getter;
import java.time.Instant;

@Getter
public class AuthResponse {
    // getters
    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;

    public AuthResponse(int status, String errorCode, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }
}
