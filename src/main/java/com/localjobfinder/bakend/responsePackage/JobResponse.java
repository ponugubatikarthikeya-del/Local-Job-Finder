package com.localjobfinder.bakend.responsePackage;

import com.localjobfinder.bakend.enums.JobStatusEnum;
import com.localjobfinder.bakend.enums.JobTypeEnum;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class JobResponse {

    private final Long id;
    private final Long userId;
    private final String title;
    private final String description;
    private final String category;
    private final BigDecimal payAmount;
    private final JobTypeEnum jobType;
    private final Double latitude;
    private final Double longitude;
    private final String address;
    private final JobStatusEnum status;
    private final LocalDateTime jobTime;

    public JobResponse(Long id, Long userId, String title, String description, String category,
                       BigDecimal payAmount, JobTypeEnum jobType, Double latitude, Double longitude,
                       String address, JobStatusEnum status, LocalDateTime jobTime) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.payAmount = payAmount;
        this.jobType = jobType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.status = status;
        this.jobTime = jobTime;
    }
}
