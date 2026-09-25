package com.localjobfinder.bakend.dbEntity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "shed_lock")
public class ShedLockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",nullable = false,length = 64 )
    private String name;

    @Column(name = "lock_type", nullable = false)
    private Instant lockUntil;

    @Column(name = "locked_at",nullable = false)
    private Instant lockedAt;

    @Column(name = "locked_by",nullable = false,length = 255)
    private String lockedBy;

}
