package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.TokenEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<TokenEntity, Long> {
    List<TokenEntity> findByUser(UserEntity user);
    Optional<TokenEntity> findByHashResetKey(String hashResetKey);
}
