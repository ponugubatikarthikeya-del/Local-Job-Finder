package com.localjobfinder.bakend.dtoPackage;

import com.localjobfinder.bakend.enums.JobStatusEnum;
import com.localjobfinder.bakend.enums.JobTypeEnum;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class JobCardDto {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String category;
    private BigDecimal payAmount;
    private JobTypeEnum jobType;
    private String address;
    private JobStatusEnum status;
    private Double distance;
}

