package com.localjobfinder.bakend.components.schedulers;

import com.localjobfinder.bakend.enums.AccountStatusEnum;
import com.localjobfinder.bakend.repository.FcmTokenRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class AccountCleanupScheduler {

    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;

    @Autowired
    public AccountCleanupScheduler(UserRepository userRepository, FcmTokenRepository fcmTokenRepository) {
        this.userRepository = userRepository;
        this.fcmTokenRepository = fcmTokenRepository;
    }

    // Daily maintenance at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeExpiredUnverifiedAccounts() {
        executeCleanup();
    }

    // CATCH-UP LOGIC: Runs automatically as soon as the server boots up successfully
    @EventListener(ApplicationReadyEvent.class)
    public void cleanupOnStartup() {
        executeCleanup();
    }

    @Transactional
    private void executeCleanup() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        userRepository.deleteByAccountStatusAndCreatedAtBefore(
                AccountStatusEnum.UNVERIFIED,
                cutoff
        );
    }


    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Kolkata") // Runs at exactly 2:00 AM IST
//    @Scheduled(cron = "*/10 * * * * ?") // 10 sec test
    @Transactional
    public void purgeExpiredFcmTokens() {
        // Correct math calculation step using Instants for historical boundary comparisons
        Instant limit = Instant.now().minus(90, ChronoUnit.DAYS);

        fcmTokenRepository.deleteByUpdatedAtBefore(limit);
        log.info("Cron job executed: Cleaned up expired FCM tokens older than 90 days.");
    }

}
