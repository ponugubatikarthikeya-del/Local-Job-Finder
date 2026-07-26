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
    AUTH_010("Missing or invalid Authorization header"),

    // 🔐 AUTHORIZATION ERRORS
    AUTHZ_001("Access denied."),
    AUTHZ_002("Insufficient permissions."),

    // ==========================================
    // AUTH & OAUTH ERROR CODES
    // ==========================================
    OAUTH_001("Invalid or expired Google ID Token."),
    OAUTH_002("Failed to verify Google Token: "),
    OAUTH_003("Provider identifier and authentication keys cannot be empty."),
    OAUTH_004("Unsupported OAuth provider: "),
    OAUTH_005("Invalid or malformed Google ID Token signature."),
    OAUTH_006("Google ID Token validation rejected: "),
    OAUTH_007("Empty payload body from GitHub profile query."),
    OAUTH_008("Unable to resolve a valid profile email address from your GitHub account."),
    OAUTH_009("GitHub OAuth Verification failed: "),
    OAUTH_010("Unable to retrieve verified email from account."),
    OAUTH_011("Facebook OAuth Verification failed: ."),

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
    GEN_001("Internal server error."),

    JOB_001("ob context tracking parameter not found."),
    JOB_002("Reviews can only be posted after the job is completely finalized."),
    JOB_003("You have already submitted a review for this job workspace."),
    JOB_004("No active worker placement records found for this workspace."),

    RREVIW_001("Access Denied: You are not authorized to review this job performance.");

    private final String message;

    ErrorMsg(String message) {
        this.message = message;
    }

}
