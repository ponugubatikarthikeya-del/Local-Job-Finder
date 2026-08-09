package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.enums.AccountStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Transactional
    Optional<UserEntity> findByEmail(String email);

    // Removes all unverified accounts created before the specified cutoff date/time
    @Modifying
    @Transactional
    void deleteByAccountStatusAndCreatedAtBefore(AccountStatusEnum accountStatus, Instant cutoff);

}

