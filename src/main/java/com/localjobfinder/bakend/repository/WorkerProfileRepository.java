package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.WorkerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfileEntity,Long> {

}
