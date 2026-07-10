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

public class JobsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private UserEntity user;

    @Column(name = "title", nullable = false, length = 150) // Matched VARCHAR(150)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT") // Matched TEXT schema type
    private String description;

    @Column(name = "category", length = 100) // Matched VARCHAR(100)
    private String category;

    @Column(name = "pay_amount", precision = 10, scale = 2) // Matched DECIMAL(10,2)
    private BigDecimal payAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type")
    private JobTypeEnum jobType;

    // Fixed: Changed from Double to BigDecimal and added precision/scale mappings
    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    // Fixed: Changed from Double to BigDecimal and added precision/scale mappings
    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;

    @Column(name = "address", length = 255) // Matched VARCHAR(255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatusEnum status;

    @Column(name = "job_time")
    private LocalDateTime jobTime;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}
