package com.localjobfinder.bakend.redis;

import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.dtoPackage.UserDto;
import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RedisSerializerTest {

    @Test
    void roundTripsCachedJobResponseWithBigDecimalAndInstant() {
        JobDto job = new JobDto();
        job.setId(42L);
        job.setPayAmount(new BigDecimal("125.50"));
        job.setDistance("1.2km");

        Instant timestamp = Instant.parse("2026-09-30T12:00:00Z");
        JobPagingResponse<PagedResponse<JobDto>> response = new JobPagingResponse<>(
                timestamp,
                200,
                "OK",
                "success",
                "/jobs/filter",
                new PagedResponse<>(List.of(job), 0, 10, 1, 1, true)
        );

        RedisSerializer<Object> serializer = new RedisConfig().redisJsonSerializer();
        Object result = serializer.deserialize(serializer.serialize(response));

        JobPagingResponse<?> decodedResponse = assertInstanceOf(JobPagingResponse.class, result);
        assertEquals(timestamp, decodedResponse.getTimestamp());

        PagedResponse<?> decodedPage = assertInstanceOf(PagedResponse.class, decodedResponse.getData());
        JobDto decodedJob = assertInstanceOf(JobDto.class, decodedPage.getContent().getFirst());
        assertEquals(new BigDecimal("125.50"), decodedJob.getPayAmount());
        assertEquals("1.2km", decodedJob.getDistance());
    }

    @Test
    void roundTripsCollectionsInCachedProfileDtos() {
        UserDto user = new UserDto();
        user.setSkills(List.of("Java", "Spring"));

        RedisSerializer<Object> serializer = new RedisConfig().redisJsonSerializer();
        Object result = serializer.deserialize(serializer.serialize(user));

        UserDto decodedUser = assertInstanceOf(UserDto.class, result);
        assertEquals(List.of("Java", "Spring"), decodedUser.getSkills());
    }
}
