package com.localjobfinder.bakend.notification.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAcceptNotificationDto {
    private Long jobId;
    private String jobTitle;

    // Job Owner Details
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;
}
