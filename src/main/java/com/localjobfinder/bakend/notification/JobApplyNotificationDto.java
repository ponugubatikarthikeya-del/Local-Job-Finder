package com.localjobfinder.bakend.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplyNotificationDto {
    private Long jobId;
    private String jobTitle;

    // Applicant Details
    private Long applicantId;
    private String applicantName;
    private String applicantPhone;
}