package com.localjobfinder.bakend.dbEntity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "user_ratings_summary",indexes = {
        @Index(name = "idx_summary_user_id",columnList = "user_id")
})
public class UserRatingsEntity {

    @Id
    private Long userId;

    @Column(name = "total_stars", nullable = false)
    private Long totalStars = 0L;

    @Column(name = "review_count", nullable = false)
    private Long reviewCount = 0L;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

}
