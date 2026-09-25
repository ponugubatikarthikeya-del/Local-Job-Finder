package com.localjobfinder.bakend.repository;

import com.localjobfinder.bakend.dbEntity.JobsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobsRepository extends JpaRepository<JobsEntity, Long>, JpaSpecificationExecutor<JobsEntity> {

    // Optimized for Production Paging
    @Query("SELECT j FROM JobsEntity j WHERE j.user.id = :userId AND j.status <> 'DELETED'")
    Page<JobsEntity> findByUserId(@Param("userId") Long userId, Pageable pageable);


//
//    String SELECT_COLUMNS =
//            "j.id, j.user_id AS userId, u.username AS username, j.title, j.description, j.category, " +
//                    "j.pay_amount AS payAmount, j.job_type AS jobType, j.latitude, j.longitude, j.address, " +
//                    "j.status, j.job_time AS jobTime, j.created_at AS createdAt, " +
//                    "(ST_Distance_Sphere(j.location, ST_SRID(POINT(:lng, :lat), 4326)) / 1000.0) AS distance ";
//
//    String FROM_TABLES =
//            "FROM jobs j " +
//                    "JOIN users u ON j.user_id = u.id ";
//
//    String WHERE_CLAUSE =
//            "j.status = 'OPEN' AND " +
//                    "ST_Distance_Sphere(j.location, ST_SRID(POINT(:lng, :lat), 4326)) <= (:radius * 1000.0) " +
//                    "AND (:category IS NULL OR LOWER(j.category) = LOWER(:category)) " +
//                    "AND (:salary IS NULL OR j.pay_amount >= :salary) " +
//                    "AND (:searchKey IS NULL OR " +
//                    "LOWER(j.title) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR " +
//                    "LOWER(j.description) LIKE LOWER(CONCAT('%', :searchKey, '%')) OR " +
//                    "LOWER(j.category) LIKE LOWER(CONCAT('%', :searchKey, '%'))) ";
//
//    @Query(value = "SELECT " + SELECT_COLUMNS + FROM_TABLES + "WHERE " + WHERE_CLAUSE +
//            "ORDER BY j.created_at DESC, j.id DESC",
//            countQuery = "SELECT COUNT(*) " + FROM_TABLES + "WHERE " + WHERE_CLAUSE,
//            nativeQuery = true)
//
//    Page<JobProjection> findNearbyAndFilteredJobs(
//            @Param("lat") BigDecimal lat,
//            @Param("lng") BigDecimal lng,
//            @Param("radius") Double radius,
//            @Param("category") String category,
//            @Param("salary") Double salary,
//            @Param("searchKey") String searchKey,
//            Pageable pageable
//    );

}

