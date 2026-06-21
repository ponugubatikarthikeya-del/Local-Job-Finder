package com.localjobfinder.bakend.enums;

import lombok.Getter;

@Getter
public enum ErrorMsg {

    // 🔐 VALIDATION ERRORS
    VAL_001("Bad request."),
    VAL_002("Validation failed."),
    VAL_003("Invalid input fields."),

    // 🔐 AUTHENTICATION ERRORS
    AUTH_001("Invalid credentials."),
    AUTH_002("Authentication failed."),
    AUTH_003("Account not verified."),
    AUTH_004("Unauthorized access."),
    AUTH_005("Account already exists."),
    AUTH_006("Email not Register with us."),
    AUTH_007("password is incorrect."),
    AUTH_008("User Account not exists."),
    AUTH_009("password is does not match."),

    // 🔐 AUTHORIZATION ERRORS
    AUTHZ_001("Access denied."),
    AUTHZ_002("Insufficient permissions."),

    // 🔐 TOKEN ERRORS
    TOKEN_001("Authorization header missing."),
    TOKEN_002("Token is invalid."),
    TOKEN_003("Token expired or invalid."),

    OTP_001("OTP expired."),
    OTP_002("OTP is invalid."),

    // 🧠 BUSINESS ERRORS
    BUS_001("Resource not found."),
    BUS_002("Duplicate resource."),
    BUS_003("Job is already closed."),
    BUS_004("Already applied for this job."),
    BUS_005("Invalid job state."),
    BUS_006("Profile is incomplete."),
    BUS_007("Access denied for this resource."),

    // ⚙️ SYSTEM ERRORS
    SYS_001("Database error occurred."),
    SYS_002("File upload failed."),

    // 🌐 GENERAL
    GEN_001("Internal server error.");

    private final String message;

    ErrorMsg(String message) {
        this.message = message;
    }

}
