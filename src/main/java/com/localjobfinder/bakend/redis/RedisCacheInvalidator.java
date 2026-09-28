package com.localjobfinder.bakend.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RedisCacheInvalidator {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheInvalidator.class);

    private final CacheManager cacheManager;

    public RedisCacheInvalidator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictAfterCommit(String... cacheNames) {
        String[] names = cacheNames.clone();
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evict(names);
                }
            });
            return;
        }

        evict(names);
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
