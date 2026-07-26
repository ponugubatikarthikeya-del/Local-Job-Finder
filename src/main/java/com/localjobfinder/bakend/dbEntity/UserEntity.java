package com.localjobfinder.bakend.dbEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.localjobfinder.bakend.enums.AccountStatusEnum;
import com.localjobfinder.bakend.OauthPackage.AuthLoginEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = true) // Must be NULLABLE for Google users!
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthLoginEnum authProvider; // "LOCAL", "GOOGLE", or "HYBRID"

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name ="github_id",unique = true)
    private String githubId;

    @Column(name = "facebook_id",unique = true)
    private String facebookId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatusEnum accountStatus;

//    @Column(name = "fcm_token", length = 512) // FCM tokens can be long strings
//    private String fcmToken;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;

    // Refresh tokens are stored in a separate table; keep relation for convenience
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TokenEntity> refreshTokens = new ArrayList<>();

//    @Enumerated(EnumType.STRING)
//    @Column(name = "role", nullable = false)
//    private UserRoleEnum role = UserRoleEnum.WORKER;
//
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private ProfilesEntity profile;
//
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private OwnerProfileEntity ownerProfile;

}
