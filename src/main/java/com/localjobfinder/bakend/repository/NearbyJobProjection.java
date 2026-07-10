package com.localjobfinder.bakend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface NearbyJobProjection {

    // Standard job properties matching columns from 'j.*'
    Long getId();
    String getTitle();
    String getDescription();
    String getCategory();
    BigDecimal getPayAmount();
    String getJobType();
    Double getLatitude();
    Double getLongitude();
    String getAddress();
    String getStatus();
    LocalDateTime getJobTime();

    // WHY: This matches your dynamic "(6371 * ...) AS distance" column alias
    Double getDistance();
}