package com.localjobfinder.bakend;


import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class FlywayConfig {

    @Bean
//    @Profile("dev") // 👈 CRITICAL: Run this on dev only! Do not run in production.
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            // Cleans up the failed migration state entries and updates checksums
            flyway.repair();
            // Continues with the schema updates normally
            flyway.migrate();
        };
    }
}