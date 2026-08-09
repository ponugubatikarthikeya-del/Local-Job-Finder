package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.JobsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface JobsRepository extends JpaRepository<JobsEntity, Long> {

    // Optimized for Production Paging
    @Query("SELECT j FROM JobsEntity j WHERE j.user.id = :userId AND j.status <> 'DELETED'")
    Page<JobsEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);


}

