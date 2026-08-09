package com.localjobfinder.bakend.notification.job;

import lombok.Getter;

@Getter
public class JobNotificationEvent {
    private final Long targetUserId;
    private final String title;
    private final String message;
    private final String notificationType; // "JOB_APPLIED" or "JOB_ACCEPTED"
    private final Object detailsPayload;   // Holds either JobApplyNotificationDto or JobAcceptNotificationDto

    public JobNotificationEvent(Long targetUserId, String title, String message, String notificationType, Object detailsPayload) {
        this.targetUserId = targetUserId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.detailsPayload = detailsPayload;
    }
}