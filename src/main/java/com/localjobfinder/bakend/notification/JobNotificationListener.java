package com.localjobfinder.bakend.notification;

import com.google.firebase.FirebaseApp;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
public class JobNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(JobNotificationListener.class);

    private UserRepository userRepository;
    private FirebaseConfiguration firebaseConfiguration;

    @Autowired
    public JobNotificationListener(UserRepository userRepository, FirebaseConfiguration firebaseConfiguration) {
        this.userRepository = userRepository;
        this.firebaseConfiguration = firebaseConfiguration;
    }

    @Async
    @EventListener
    public void handleJobNotificationEvent(JobNotificationEvent event) throws CustomException {

        UserEntity user = userRepository.findById(event.getTargetUserId()).orElse(null);

        if (user != null && user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {

            firebaseConfiguration.sendPushNotification(
                    user.getFcmToken(),
                    event.getTitle(),
                    event.getMessage(),
                    event.getNotificationType(),
                    event.getDetailsPayload()
            );

        }
    }
}
