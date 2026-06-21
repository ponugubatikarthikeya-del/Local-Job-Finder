package com.localjobfinder.bakend.dbEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.localjobfinder.bakend.enums.UserAvailabilityStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "worker_profiles")
public class WorkerProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference
    private UserEntity user;

    @Column(name = "skills",columnDefinition = "json")
    private String skills;

    @Column(name = "experience_level")
    private String experienceLevel;

    @Column(name = "rating")
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_jobs_completed")
    private Integer totalJobsCompleted = 0 ;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status",nullable = false)
    private UserAvailabilityStatusEnum availabilityStatus = UserAvailabilityStatusEnum.AVAILABLE;

}
