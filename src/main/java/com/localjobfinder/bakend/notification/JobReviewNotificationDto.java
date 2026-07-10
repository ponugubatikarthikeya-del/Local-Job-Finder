package com.localjobfinder.bakend.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobReviewNotificationDto {
    private Long jobId;
    private String jobTitle;
    private Long reviewerId;
    private String reviewerName;
    private Integer rating;
    private String comment;
}
