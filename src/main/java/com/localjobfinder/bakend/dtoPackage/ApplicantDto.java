package com.localjobfinder.bakend.dtoPackage;

import lombok.Data;

import java.time.Instant;

@Data
public class ApplicantDto {

    private Long applicationId;
    private Long userId;
    private String username;
    private String headline;
    private Instant appliedAt;

}
