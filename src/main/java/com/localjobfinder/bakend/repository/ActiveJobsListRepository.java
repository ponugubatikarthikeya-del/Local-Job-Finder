package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.ActiveJobsListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActiveJobsListRepository extends JpaRepository<ActiveJobsListEntity,Long> {

}
