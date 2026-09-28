package com.localjobfinder.bakend.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.redis.cache-monitoring",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RedisCacheMonitoring {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheMonitoring.class);

    private final RedisCacheManager cacheManager;

    public RedisCacheMonitoring(RedisCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // Statistics are per application instance and cumulative since startup or last reset.
    @Scheduled(
            fixedDelayString = "${app.redis.cache-monitoring.interval:5m}",
            initialDelayString = "${app.redis.cache-monitoring.initial-delay:1m}"
    )
    public void logCacheStatistics() {
        for (String cacheName : cacheManager.getCacheNames()) {
            if (cacheManager.getCache(cacheName) instanceof RedisCache redisCache) {
                CacheStatistics statistics = redisCache.getStatistics();
                long requests = statistics.getHits() + statistics.getMisses();
                double hitRate = requests == 0
                        ? 0.0
                        : statistics.getHits() * 100.0 / requests;

                log.info(
                        "Redis cache statistics: cache={}, hits={}, misses={}, hitRate={}%, puts={}, deletes={}, since={}",
                        cacheName,
                        statistics.getHits(),
                        statistics.getMisses(),
                        String.format("%.2f", hitRate),
                        statistics.getPuts(),
                        statistics.getDeletes(),
                        statistics.getSince()
                );
            }
        }
    }
}
