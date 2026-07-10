package com.localjobfinder.bakend.responsePackage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ViewApplicantsResponse {

    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final Object allJobs;

    // Pagination Metadata fields for Mobile UI client management
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private boolean isLast;

    public ViewApplicantsResponse(int status, String errorCode, String message, String path, Object allJobs) {
        this.timestamp = Instant.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.allJobs = allJobs;
    }

}
