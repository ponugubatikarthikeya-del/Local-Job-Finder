package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.TokenEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<TokenEntity, Long> {

    Optional<TokenEntity> findByUserAndDeviceId(UserEntity user, String deviceId);

    List<TokenEntity> findByUser(UserEntity user);

    Optional<TokenEntity> findByHashResetKey(String hashResetKey);

    void deleteByUserAndDeviceId(UserEntity user, String deviceId); // it used in when user clicks logout on their phone

    void deleteByUser(UserEntity user);  // it used in when user clicks logout to all phones

}
