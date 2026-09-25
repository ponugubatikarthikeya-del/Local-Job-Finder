package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.TokenEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Long> {

    Optional<TokenEntity> findByDeviceId(String deviceId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TokenEntity t WHERE t.user.id = :userId AND t.deviceId = :deviceId")
    Optional<TokenEntity> findByUserIdAndDeviceIdWithLock(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    Optional<TokenEntity> findByHashResetKey(String hashResetKey);

    List<TokenEntity> findByUserId(Long userId);

    /**
     * Finds all active tokens for a specific user, ordered from oldest to newest.
     * Use this method to check device limits and safely recycle the oldest session row.
     *
     * NOTE: Assumes your TokenEntity has a timestamp field (like 'createdAt' or 'id').
     * If your entity uses a different timestamp name, change 'createdAt' to match it.
     */
    List<TokenEntity> findByUserIdOrderByCreatedAtAsc(Long userId);

    // FIXED: Use user.id instead of the entire user entity object
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenEntity t WHERE t.user.id = :userId AND t.deviceId = :deviceId")
    void deleteByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);

    // FIXED: Use user.id for all-device bulk delete
    @Modifying
    @Transactional
    @Query("DELETE FROM TokenEntity t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);


    Optional<TokenEntity> findByHashRefreshTokenAndRevokedFalse(String hashRefreshToken);

}
