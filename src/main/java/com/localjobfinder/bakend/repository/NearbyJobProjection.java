package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface NearbyJobProjection {

    // Standard job properties matching columns from 'j.*'
    Long getId();
    String getTitle();
    Long getUserId();
    String getUsername();
    String getDescription();
    String getCategory();
    BigDecimal getPayAmount();
    String getJobType();
    Double getLatitude();
    Double getLongitude();
    String getAddress();
    String getStatus();
    LocalDateTime getJobTime();

    // Distance is returned by MySQL ST_Distance_Sphere in kilometers.
    Double getDistance();
}