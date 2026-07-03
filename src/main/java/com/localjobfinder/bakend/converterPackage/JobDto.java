package com.localjobfinder.bakend.converterPackage;

import com.localjobfinder.bakend.enums.JobStatusEnum;
import com.localjobfinder.bakend.enums.JobTypeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class JobDto {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String category;
    private BigDecimal payAmount;
    private JobTypeEnum jobType;
    private Double latitude;
    private Double longitude;
    private String address;
    private JobStatusEnum status;
    private LocalDateTime jobTime;
    private LocalDateTime createdAt;
}
