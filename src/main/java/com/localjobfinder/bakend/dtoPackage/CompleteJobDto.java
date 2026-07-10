package com.localjobfinder.bakend.dtoPackage;

import com.localjobfinder.bakend.enums.JobStatusEnum;
import com.localjobfinder.bakend.enums.JobTypeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CompleteJobDto {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String description;
    private String category;
    private BigDecimal payAmount;
    private JobTypeEnum jobType;
    private String address;
    private JobStatusEnum status;
    private LocalDateTime jobTime;
    private LocalDateTime createdAt;
}
