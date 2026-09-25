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

@Repository
public interface ActiveJobsListRepository extends JpaRepository<ActiveJobsListEntity, Long> {

    String SELECT_NEARBY_COLUMNS =
            "j.id, j.user_id AS userId, u.username AS username, j.title, j.description, j.category, " +
            "j.pay_amount AS payAmount, j.job_type AS jobType, j.latitude, j.longitude, j.address, " +
            "j.status, j.job_time AS jobTime, " +
            "(ST_Distance_Sphere(j.location, ST_SRID(POINT(:workerLng, :workerLat), 4326)) / 1000.0) AS distance ";

    String ACTIVE_JOBS_FROM =
            "FROM active_listings al " +
            "JOIN jobs j ON al.job_id = j.id " +
            "JOIN users u ON j.user_id = u.id ";

    String SPATIAL_RADIUS_FILTER =
            "j.location IS NOT NULL AND " +
            "ST_Distance_Sphere(j.location, ST_SRID(POINT(:workerLng, :workerLat), 4326)) <= (:radius * 1000.0) ";

    @Query(value = "SELECT " + SELECT_NEARBY_COLUMNS + ACTIVE_JOBS_FROM +
            "WHERE j.status = 'OPEN' AND " + SPATIAL_RADIUS_FILTER +
            "ORDER BY j.created_at DESC, j.id DESC",
            countQuery = "SELECT COUNT(*) " + ACTIVE_JOBS_FROM +
                    "WHERE j.status = 'OPEN' AND " + SPATIAL_RADIUS_FILTER,
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

    @Query(value = "SELECT " + SELECT_NEARBY_COLUMNS + ACTIVE_JOBS_FROM +
            "WHERE j.status = 'OPEN' AND " + SPATIAL_RADIUS_FILTER +
            "AND (:category IS NULL OR LOWER(j.category) = LOWER(:category)) " +
            "AND (:searchKey IS NULL OR " +
            "LOWER(j.title) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR " +
            "LOWER(j.description) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR " +
            "LOWER(j.category) LIKE LOWER(CONCAT('%', :searchKey, '%'))) " +
            "ORDER BY j.created_at DESC, j.id DESC",
            countQuery = "SELECT COUNT(*) " + ACTIVE_JOBS_FROM +
                    "WHERE j.status = 'OPEN' AND " + SPATIAL_RADIUS_FILTER +
                    "AND (:category IS NULL OR LOWER(j.category) = LOWER(:category)) " +
                    "AND (:searchKey IS NULL OR " +
                    "LOWER(j.title) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR " +
                    "LOWER(j.description) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR " +
                    "LOWER(j.category) LIKE LOWER(CONCAT('%', :searchKey, '%')))",
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
