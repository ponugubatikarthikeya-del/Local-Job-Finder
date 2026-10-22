package com.localjobfinder.bakend.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    @Value("${spring.cache.redis.time-to-live:10m}")
    private Duration cacheTimeToLive;

    @Value("${app.redis.cache-key-prefix:local-job-finder:local:v1}")
    private String cacheKeyPrefix;

    @Value("${app.redis.cache-clear-scan-batch-size:1000}")
    private int cacheClearScanBatchSize;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            RedisSerializer<Object> redisJsonSerializer
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(redisJsonSerializer);
        template.setHashValueSerializer(redisJsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            RedisSerializer<Object> redisJsonSerializer
    ) {
        if (cacheTimeToLive.isNegative() || cacheTimeToLive.isZero()) {
            throw new IllegalStateException("Redis cache TTL must be greater than zero");
        }
        if (cacheClearScanBatchSize <= 0) {
            throw new IllegalStateException("Redis cache clear SCAN batch size must be greater than zero");
        }
        if (cacheKeyPrefix.isBlank()) {
            throw new IllegalStateException("Redis cache key prefix must not be blank");
        }

        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(cacheTimeToLive)
                        .disableCachingNullValues()
                        .computePrefixWith(cacheName -> cacheKeyPrefix + ":" + cacheName + "::")
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(redisJsonSerializer)
                        );

        RedisCacheWriter cacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(
                connectionFactory,
                BatchStrategies.scan(cacheClearScanBatchSize)
        );

        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(config)
                .enableStatistics()
                .transactionAware()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }

    @Bean
    public RedisSerializer<Object> redisJsonSerializer() {
        // Define safe polymorphic type validation for your app packages
        var typeValidator = org.springframework.expression.common.LiteralExpression.class; // or standard validator
        // Better yet, use Jackson 3's BasicPolymorphicTypeValidator:
        var validator = tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.localjobfinder.bakend.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.math.")
                .build();

        return org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
                .builder()
                .enableDefaultTyping(validator)
                .build();
    }
}