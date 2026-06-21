package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.PostJobsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JobsRepository extends JpaRepository<PostJobsEntity, Long> {

}
