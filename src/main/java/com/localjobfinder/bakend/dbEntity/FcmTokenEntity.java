package com.localjobfinder.bakend.dbEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_fcm_tokens")
@Data
public class FcmTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Changed from @OneToOne
    @JsonBackReference
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "device_token", nullable = false, unique = true)
    private String deviceToken;
}