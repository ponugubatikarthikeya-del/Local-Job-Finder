package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.ApplicationJobsEntity;
import com.localjobfinder.bakend.enums.ApplicationStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationJobsRepository extends JpaRepository<ApplicationJobsEntity, Long> {

    @Query("SELECT a FROM ApplicationJobsEntity a WHERE a.user.id = :userId AND a.job.id = :jobId")
    Optional<ApplicationJobsEntity> findByUserIdAndJobId(@Param("userId") Long userId, @Param("jobId") Long jobId);

    @Query("SELECT a FROM ApplicationJobsEntity a WHERE a.user.id = :userId")
    List<ApplicationJobsEntity> findByUserId(@Param("userId") Long userId);

    // Optimized for Production Paging
    @Query("SELECT a FROM ApplicationJobsEntity a WHERE a.job.id = :jobId")
    Page<ApplicationJobsEntity> findByJobId(@Param("jobId") Long jobId, Pageable pageable);

    @Modifying
    @Query("UPDATE ApplicationJobsEntity a SET a.status = :status WHERE a.job.id = :jobId AND a.user.id <> :acceptedUserId")
    void updateStatusForOthers(
            @Param("jobId") Long jobId,
            @Param("acceptedUserId") Long acceptedUserId,
            @Param("status") ApplicationStatusEnum status
    );

    @Query("SELECT a FROM ApplicationJobsEntity a WHERE a.job.id = :jobId AND a.status = 'ACCEPTED'")
    Optional<ApplicationJobsEntity> findAcceptedApplicant(@Param("jobId") Long jobId);

}
