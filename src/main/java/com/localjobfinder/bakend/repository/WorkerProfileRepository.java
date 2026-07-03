package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.ProfilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerProfileRepository extends JpaRepository<ProfilesEntity,Long> {

}
