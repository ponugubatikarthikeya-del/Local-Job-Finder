package com.localjobfinder.bakend.schedule;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import javax.sql.DataSource;

@Configuration
@EnableScheduling      // 🚀 Turns on Spring Boot background scheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m") // 🚀 Turns on distributed locking
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime() // Uses the central database time to prevent clock-drift bugs between application servers
                        .build()
        );
    }

}