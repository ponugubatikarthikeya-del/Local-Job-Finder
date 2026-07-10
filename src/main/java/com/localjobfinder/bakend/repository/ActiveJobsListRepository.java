package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.ActiveJobsListEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ActiveJobsListRepository extends JpaRepository<ActiveJobsListEntity, Long> {

    //address filter query

    @Query(value = "SELECT j.id, j.title, j.description, j.category, j.pay_amount AS payAmount, j.job_type AS jobType, " +
            "j.latitude, j.longitude, j.address, j.status, j.job_time AS jobTime, " +
            "(6371 * acos(cos(radians(:workerLat)) * cos(radians(j.latitude)) * " +
            "cos(radians(j.longitude) - radians(:workerLng)) + " +
            "sin(radians(:workerLat)) * sin(radians(j.latitude)))) AS distance " +
            "FROM active_listings al " +
            "JOIN jobs j ON al.job_id = j.id " +
            "WHERE j.status = 'OPEN' " +
            "HAVING distance <= :radius ",
            countQuery = "SELECT count(*) FROM active_listings al " +
                    "JOIN jobs j ON al.job_id = j.id " +
                    "WHERE j.status = 'OPEN' AND " +
                    "(6371 * acos(cos(radians(:workerLat)) * cos(radians(j.latitude)) * " +
                    "cos(radians(j.longitude) - radians(:workerLng)) + " +
                    "sin(radians(:workerLat)) * sin(radians(j.latitude)))) <= :radius",
            nativeQuery = true)
    Page<NearbyJobProjection> findNearbyActiveJobs(
            @Param("workerLat") BigDecimal workerLat,
            @Param("workerLng") BigDecimal workerLng,
            @Param("radius") Double radius,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM ActiveJobsListEntity al WHERE al.job.id = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);

    //search query

    @Query(value = "SELECT j.id, j.title, j.description, j.category, j.pay_amount AS payAmount, j.job_type AS jobType, " +
            "j.latitude, j.longitude, j.address, j.status, j.job_time AS jobTime, " +
            "(6371 * acos(cos(radians(:workerLat)) * cos(radians(j.latitude)) * " +
            "cos(radians(j.longitude) - radians(:workerLng)) + " +
            "sin(radians(:workerLat)) * sin(radians(j.latitude)))) AS distance " +
            "FROM active_listings al " +
            "JOIN jobs j ON al.job_id = j.id " +
            "WHERE j.status = 'OPEN' " +
            "AND (:category IS NULL OR j.category = :category) " +
            "AND (:searchKey IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR LOWER(j.description) LIKE LOWER(CONCAT('%', :searchKey, '%'))) " +
            "HAVING distance <= :radius",
            countQuery = "SELECT count(*) FROM active_listings al " +
                    "JOIN jobs j ON al.job_id = j.id " +
                    "WHERE j.status = 'OPEN' " +
                    "AND (:category IS NULL OR j.category = :category) " +
                    "AND (:searchKey IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR LOWER(j.description) LIKE LOWER(CONCAT('%', :searchKey, '%'))) " +
                    "AND (6371 * acos(cos(radians(:workerLat)) * cos(radians(j.latitude)) * " +
                    "cos(radians(j.longitude) - radians(:workerLng)) + " +
                    "sin(radians(:workerLat)) * sin(radians(j.latitude)))) <= :radius",
            nativeQuery = true)
    Page<NearbyJobProjection> searchNearbyJobs(
            @Param("workerLat") BigDecimal workerLat,
            @Param("workerLng") BigDecimal workerLng,
            @Param("radius") Double radius,
            @Param("searchKey") String searchKey,
            @Param("category") String category,
            Pageable pageable
    );

}
