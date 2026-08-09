package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.FcmTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface FcmTokenRepository extends JpaRepository<FcmTokenEntity, Long> {

    List<FcmTokenEntity> findByUserId(Long userId);

    @Modifying
    void deleteByDeviceToken(String deviceToken);

    @Modifying
    void deleteByUserIdAndDeviceToken(Long userId, String deviceToken);

    @Modifying
    void deleteByUpdatedAtBefore(Instant threshold);

    // 💡 FIX: Add this explicit query to safely clear old FCM tokens without breaking auth sessions
    @Modifying
    @Query("DELETE FROM FcmTokenEntity f WHERE f.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}