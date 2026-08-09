package com.localjobfinder.bakend.dbEntity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.localjobfinder.bakend.enums.UserAvailabilityStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "user_profiles")
public class ProfilesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference
    private UserEntity user;

    @Column(name = "headline")
    private String headline;

    @Column(name = "skills",columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> skills;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "city_name")
    private String cityName;

    @Column(name = "rating")
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_jobs_completed")
    private Integer totalJobsCompleted = 0 ;

    @Column(name = "user_description")
    private String userDescription;

    // Fixed: Changed from Double to BigDecimal and added precision/scale mappings
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    // Fixed: Changed from Double to BigDecimal and added precision/scale mappings
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status",nullable = false)
    private UserAvailabilityStatusEnum availabilityStatus = UserAvailabilityStatusEnum.AVAILABLE;

}
