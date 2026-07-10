package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.ProfilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfilesEntity,Long> {

    Optional<ProfilesEntity> findByUser_Id(Long userId);
}
