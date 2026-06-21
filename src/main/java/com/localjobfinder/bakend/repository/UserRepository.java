package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByOtp(int otp);

    Optional<UserEntity> findByResetKey(String resetKey);

    Optional<UserEntity> findByRefreshToken(String refreshToken);
}
