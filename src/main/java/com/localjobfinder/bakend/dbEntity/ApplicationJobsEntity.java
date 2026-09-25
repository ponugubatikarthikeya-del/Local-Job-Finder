package com.localjobfinder.bakend.dbEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import com.localjobfinder.bakend.enums.ApplicationStatusEnum;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Data
@Entity
@Table(name = "job_applications")
public class ApplicationJobsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore
    private JobsEntity job;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatusEnum status = ApplicationStatusEnum.APPLIED;

    @CreationTimestamp
    @Column(name = "created_at",updatable = false)
    private Instant createdAt;

}
