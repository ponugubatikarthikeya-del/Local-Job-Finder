package com.localjobfinder.bakend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LocalJobFinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalJobFinderApplication.class, args);
    }

}
