package com.localjobfinder.bakend.dtoPackage;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class UserDto {
   private Long id;
   private String username;
   private String email;
   private String phone;
   private String headline;
   private List<String> skills;
   private String dateOfBirth;
   private String cityName;
   private BigDecimal rating;
   private Integer totalJobsCompleted;
   private String userDescription;
   private Instant createdAt;
}
