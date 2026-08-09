package com.localjobfinder.bakend.notification;

import com.localjobfinder.bakend.dbEntity.FcmTokenEntity;

import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.notification.job.JobNotificationEvent;
import com.localjobfinder.bakend.repository.FcmTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;


@Component
public class JobNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(JobNotificationListener.class);

    private final  FirebaseConfiguration firebaseConfiguration;
    private final FcmTokenRepository fcmTokenRepository;

    @Autowired
    public JobNotificationListener(
                                   FirebaseConfiguration firebaseConfiguration,FcmTokenRepository fcmTokenRepository) {
        this.firebaseConfiguration = firebaseConfiguration;
        this.fcmTokenRepository = fcmTokenRepository;
    }


   @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJobNotificationEvent(JobNotificationEvent event) throws CustomException {
        // 1. Fetch all active device tokens associated with this specific user
        List<FcmTokenEntity> userTokens = fcmTokenRepository.findByUserId(event.getTargetUserId());

        if (userTokens.isEmpty()) {
            return; // No registered devices found for this user, exit early safely
        }

        // 2. Loop through all registered devices and send notifications to each
        for (FcmTokenEntity tokenEntity : userTokens) {
            try {
                firebaseConfiguration.sendPushNotification(
                        tokenEntity.getDeviceToken(),
                        event.getTitle(),
                        event.getMessage(),
                        event.getNotificationType(),
                        event.getDetailsPayload()
                );
            } catch (Exception e) {
                log.error("Failed to send push notification to token: " + tokenEntity.getDeviceToken(), e);

                // If the exception indicates an invalid/expired registration, clean it up:
                // fcmTokenRepository.deleteByDeviceToken(tokenEntity.getDeviceToken());

//                if ("registration-token-not-registered".equals(e.getMessagingErrorCode().name().toLowerCase())) {
//                    fcmTokenRepository.deleteByDeviceToken(tokenEntity.getDeviceToken());
//                }
            }
        }

    }

}
