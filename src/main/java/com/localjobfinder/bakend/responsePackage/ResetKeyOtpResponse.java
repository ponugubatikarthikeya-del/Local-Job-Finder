package com.localjobfinder.bakend.responsePackage;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ResetKeyOtpResponse {
    // getters
    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final String resetKey;

    public ResetKeyOtpResponse(int status, String errorCode,
                         String message, String path, String resetKey) {
        this.timestamp = Instant.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.resetKey = resetKey;
    }
}
