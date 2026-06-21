package com.localjobfinder.bakend.dbEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "active_listings")
public class ActiveJobsListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long list_id;

    // CONNECTION 1: Links to the Permanent Job record
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore // We usually don't want to reserialize the whole job inside the listing
    private PostJobsEntity job;

    // CONNECTION 2: Links to the Owner (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity owner;

    // Spatial coordinate (MySQL POINT)
    @Column(columnDefinition = "POINT", nullable = false)
    private Point location;

    private BigDecimal payAmount;
    private String category;

    @Column(name = "expiry_at")
    private LocalDateTime expiryAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

}
