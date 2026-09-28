package com.localjobfinder.bakend.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

@Component("searchCacheKeyGenerator")
public class SearchCacheKeyGenerator implements KeyGenerator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object generate(@NonNull Object target, @NonNull Method method, Object... params) {
        try {
            List<Object> keyParts = List.of(
                    method.toGenericString(),
                    params == null ? List.of() : Arrays.asList(params)
            );
            byte[] serializedKey = objectMapper.writeValueAsBytes(keyParts);
            String hash = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(serializedKey)
            );
            return "jobs:search:" + hash;
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "Could not generate cache key", ex
            );
        }
    }
}