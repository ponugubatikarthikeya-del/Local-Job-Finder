package com.localjobfinder.bakend.responsePackage;

import com.localjobfinder.bakend.dtoPackage.JobDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailsResponse {
    private int status;
    private String errorName;
    private String message;
    private String path;
    private JobDto jobDetails;
}