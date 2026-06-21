package com.localjobfinder.bakend.apiDataPackage;

import com.localjobfinder.bakend.enums.JobTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class PostJobRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "description is required")
    private String description;

    @NotBlank(message = "category is required")
    private String category;

    @NotNull(message = "pay amount is required")
    private BigDecimal payAmount;

    @NotNull(message = "job type is required")
    private JobTypeEnum jobType;

    @NotNull(message = "latitude is required")
    private Double latitude;

    @NotNull(message = "longitude is required")
    private Double longitude;

    @NotBlank(message = "address is required")
    private String address;

    @NotNull(message = "job time is required")
    private LocalDateTime jobTime;

}
