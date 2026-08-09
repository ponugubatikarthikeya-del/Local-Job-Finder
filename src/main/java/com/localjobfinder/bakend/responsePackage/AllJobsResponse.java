package com.localjobfinder.bakend.responsePackage;

import com.localjobfinder.bakend.dtoPackage.JobDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class AllJobsResponse {

    private final Instant timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final List<JobDto> allJobs;

    // Pagination Metadata fields for Mobile UI client management
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private boolean isLast;

    public AllJobsResponse(int status, String errorCode,
                           String message, String path, List<JobDto> allJobs) {
        this.timestamp = Instant.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.allJobs = allJobs;
    }
}
