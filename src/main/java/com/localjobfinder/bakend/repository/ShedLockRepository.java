package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.ShedLockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShedLockRepository extends JpaRepository<ShedLockEntity, Integer> {
}
