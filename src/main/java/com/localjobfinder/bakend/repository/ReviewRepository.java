package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    boolean existsByJobIdAndReviewerId(Long jobId, Long reviewerId);

    // Production calculation: Fetch average rating for a specific user profile
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.receiver.id = :userId")
    Double getAverageRatingForUser(@Param("userId") Long userId);

}