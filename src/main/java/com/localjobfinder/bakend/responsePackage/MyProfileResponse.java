package com.localjobfinder.bakend.responsePackage;

import lombok.Getter;

import java.time.Instant;

@Getter
public class MyProfileResponse {
    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final Object data;

    public MyProfileResponse(int status, String errorCode, String message, String path, Object data) {
        this.timestamp = Instant.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.data = data;
    }
}
