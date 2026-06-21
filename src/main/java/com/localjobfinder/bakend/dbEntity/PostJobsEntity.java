package com.localjobfinder.bakend.dbEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.localjobfinder.bakend.enums.JobTypeEnum;
import com.localjobfinder.bakend.enums.JobStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "jobs",indexes = {
        @Index(name = "idx_jobs_location",columnList = "latitude,longitude"),
        @Index(name = "idx_jobs_status" , columnList = "status"),
        @Index(name = "idx_jobs_created_at" , columnList = "created_at")
})

public class PostJobsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference // This prevents the job from trying to serialize the user again
    private UserEntity user;

    @Column(name = "title")
    private String title;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "pay_amount")
    private BigDecimal pay_amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobTypeEnum jobType;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatusEnum status;

    @Column(name = "job_time")
    private LocalDateTime jobTime;

    @Column(name = "created_at",updatable = false,insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at",insertable = false,updatable = false)
    private  LocalDateTime updatedAt;

}
