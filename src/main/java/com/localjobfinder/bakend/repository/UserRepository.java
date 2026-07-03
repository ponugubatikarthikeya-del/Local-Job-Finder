package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Transactional
    Optional<UserEntity> findByEmail(String email);

//    Optional<UserEntity> findByResetKey(String resetKey);

    // refresh tokens are stored in separate table now
}

