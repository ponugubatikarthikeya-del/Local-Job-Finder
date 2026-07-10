package com.localjobfinder.bakend.dtoPackage;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicProfileDto {
    private Long id;
    private String username;
    private String headline;
    private String skills;
    private String cityName;
    private BigDecimal rating;
    private Integer totalJobsCompleted;
    private String userDescription;
}
