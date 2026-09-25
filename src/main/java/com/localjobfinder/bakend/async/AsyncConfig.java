package com.localjobfinder.bakend.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Core pool size: Threads kept alive at all times (default threads)
        executor.setCorePoolSize(5);
        // Max pool size: Max threads allowed under peak traffic
        executor.setMaxPoolSize(20);
        // Queue capacity: Requests waiting before new threads up to maxPoolSize are created
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncWork-");

        // Reject policy when queue is full and all max threads are busy:
        // CallerRunsPolicy forces the calling thread (HTTP request) to run it, applying natural backpressure
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Wait for running tasks to complete during graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    @Bean(name = "fileImageExecutor")
    public Executor fileImageExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. Core Pool Size: 4 (Safe baseline for steady I/O execution)
        executor.setCorePoolSize(4);

        // 2. Max Pool Size: 10 (Strict cap to prevent OOM errors during heavy image transformations)
        executor.setMaxPoolSize(10);

        // 3. Queue Capacity: 50 (Balanced buffer size preventing excessive memory consumption while waiting)
        executor.setQueueCapacity(50);

        // 4. Thread Metadata Prefix for identification in logs
        executor.setThreadNamePrefix("FileImageProcessor-");

        // 5. Keep-Alive: Release idle threads beyond core limit after 30 seconds
        executor.setKeepAliveSeconds(30);

        // 6. Production Rejection Policy: CallerRunsPolicy
        // Applying natural backpressure: if overloaded, the web controller thread processes it,
        // slowing down incoming uploads instead of crashing or losing data.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 7. Graceful Degradation & Termination Management
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(90); // 90 seconds gives large files enough buffer time to finish uploading

        executor.initialize();
        return executor;
    }

}
