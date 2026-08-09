package com.localjobfinder.bakend.dbEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Data
@Entity
@Table(name = "active_listings")
public class ActiveJobsListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // CONNECTION 1: Links to the Permanent Job record
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore // We usually don't want to reserialize the whole job inside the listing
    @OnDelete(action = OnDeleteAction.CASCADE)
    private JobsEntity job;

    // CONNECTION 2: Links to the Owner (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    private UserEntity owner;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

}
