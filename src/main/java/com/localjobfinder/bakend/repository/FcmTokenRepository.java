package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.FcmTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FcmTokenRepository extends JpaRepository<FcmTokenEntity, Long> {

    List<FcmTokenEntity> findByUserId(Long userId);


    @Transactional
    void deleteByDeviceToken(String deviceToken);

    @Transactional
    void deleteByUserIdAndDeviceToken(Long userId, String deviceToken);
}