package com.localjobfinder.bakend.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RedisCacheInvalidator {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheInvalidator.class);

    private final CacheManager cacheManager;

    @Autowired
    public RedisCacheInvalidator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictAfterCommit(String... cacheNames) {
        String[] names = cacheNames.clone();
        scheduleAfterCommit(() -> evict(names));
    }

    public void evictKeyAfterCommit(String cacheName, Object key) {
        scheduleAfterCommit(() -> evictKey(cacheName, key));
    }

    private void scheduleAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }

        action.run();
    }

    private void evictKey(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.error("Unable to invalidate Redis cache because it is not configured: cache={}", cacheName);
            return;
        }

        try {
            cache.evict(key);
            log.debug("Redis cache entry invalidated: cache={}", cacheName);
        } catch (RuntimeException exception) {
            log.error("Failed to invalidate Redis cache entry after data update: cache={}", cacheName, exception);
        }
    }

    private void evict(String[] cacheNames) {
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);

            if (cache == null) {
                log.error("Unable to invalidate Redis cache because it is not configured: cache={}", cacheName);
                continue;
            }

            try {
                cache.clear();
                log.info("Redis cache invalidated: cache={}", cacheName);
            } catch (RuntimeException exception) {
                log.error("Failed to invalidate Redis cache after data update: cache={}", cacheName, exception);
            }

        }
    }
}
