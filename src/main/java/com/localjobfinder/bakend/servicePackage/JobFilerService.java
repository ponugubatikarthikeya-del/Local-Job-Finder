package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.dbEntity.JobsEntity;
import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.InvalidException;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.ResourceNotFoundException;
import com.localjobfinder.bakend.apiDataPackage.JobFilterCriteria;
import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;

import com.localjobfinder.bakend.paging.SpecificationBuilder;
import com.localjobfinder.bakend.repository.*;
import com.localjobfinder.bakend.redis.JobCacheNames;
import com.localjobfinder.bakend.serviceInterfacePackage.JobFilterServiceInterface;
import com.localjobfinder.bakend.utilsPackage.MapStructMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class JobFilerService implements JobFilterServiceInterface {

    private final JobsRepository jobsRepository;
    private final UserRepository userRepository;
    private final MapStructMapper mapStructMapper;
    private final GeometryFactory geometryFactory;

    private static final Logger log = LoggerFactory.getLogger(JobFilerService.class);

    @Autowired
    public JobFilerService(JobsRepository jobsRepository, UserRepository userRepository,
                           MapStructMapper mapStructMapper, GeometryFactory geometryFactory) {
        this.jobsRepository = jobsRepository;
        this.userRepository = userRepository;
        this.mapStructMapper = mapStructMapper;
        this.geometryFactory = geometryFactory;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = JobCacheNames.JOB_SEARCH,
            key = "#userId + '_' + #criteria.search + '_' " +
                    "+ #criteria.category + '_' + #criteria.salary + '_' + #criteria.page + '_' + #criteria.size",
            sync = true
    )
    public JobPagingResponse<PagedResponse<JobDto>> filterJobs(Long userId,
                                                               JobFilterCriteria criteria, String path) {

        if (path == null || criteria.getLat() == null || criteria.getLng() == null) {
            throw new InvalidException("Search location vectors cannot be null");
        }

        validateCoordinates(criteria.getLat(), criteria.getLng());

        double radiusInKilometers = criteria.getRadius() == null ? 50.0 : criteria.getRadius();

        if (!Double.isFinite(radiusInKilometers) || radiusInKilometers <= 0) {
            throw new InvalidException("Radius must be a finite value greater than zero kilometers");
        }

        double radiusInMeters = radiusInKilometers * 1000.0;

        if (!Double.isFinite(radiusInMeters)) {
            throw new InvalidException("Radius is too large");
        }

        if (userId == null || userId <= 0) {
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        int page = criteria.getPage();

        if (page < 0) {
            throw new InvalidException("Page number cannot be negative");
        }

        int size = criteria.getSize() == 0 ? 20 : criteria.getSize();

        if (size < 10 || size > 100) {
            throw new InvalidException("Page size must be between 10 and 100");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        Point userLocation = geometryFactory.createPoint(new Coordinate(criteria.getLat().doubleValue(), criteria.getLng().doubleValue()));
        userLocation.setSRID(4326);

        Specification<JobsEntity> spec = SpecificationBuilder.filterBy(
                criteria, radiusInMeters, userLocation
        );

        Page<JobsEntity> jobsPage = jobsRepository.findAll(spec, pageable);

        log.debug("Loaded job search page from database: userId={}, page={}, size={}, resultCount={}",
                userId, page, size, jobsPage.getNumberOfElements());

        List<JobDto> dtoList = jobsPage.getContent().stream().map(job -> {
            JobDto dto = mapStructMapper.toJobDtoByJobEntity(job);
            dto.setUserId(job.getUser().getId());
            dto.setUsername(job.getUser().getUsername());
            double distanceInKilometers = calculateDistanceInKilometers(
                    criteria.getLat().doubleValue(),
                    criteria.getLng().doubleValue(),
                    job.getLatitude().doubleValue(),
                    job.getLongitude().doubleValue()
            );
            dto.setDistance(String.format(Locale.ROOT, "%.1f", distanceInKilometers));
            return dto;
        }).collect(Collectors.toList());

        PagedResponse<JobDto> pagedDataResponse = new PagedResponse<>(
                dtoList,
                jobsPage.getNumber(),
                jobsPage.getSize(),
                jobsPage.getTotalElements(),
                jobsPage.getTotalPages(),
                jobsPage.isLast()
        );

        return new JobPagingResponse<>(
                200,
                SuccessMsg.APP_200.name(),
                "Filtered listings compiled successfully",
                path,
                pagedDataResponse
        );

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = JobCacheNames.MY_JOBS,
            key = "{#userId, #page, #size, #path}"
    )
    public JobPagingResponse<PagedResponse<JobDto>> getMyJobs(Long userId, String path, int page, int size) {

        if (userId == null || userId <= 0) {
            throw new InvalidException("Invalid user token pointer");
        }

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        Page<JobsEntity> jobsPage = jobsRepository.findByUserId(userId,pageable);

        log.debug("Loaded user's job page from database: userId={}, page={}, size={}, resultCount={}",
                userId, page, size, jobsPage.getNumberOfElements());

        List<JobDto> dtoList = jobsPage.getContent().stream().map(job -> {
            JobDto dto = mapStructMapper.toJobDtoByJobEntity(job);
            dto.setUserId(userId);
            dto.setUsername(job.getUser().getUsername());
            dto.setDistance(null);
            return dto;
        }).collect(Collectors.toList());

        PagedResponse<JobDto> pagedDataResponse = new PagedResponse<>(
                dtoList,
                jobsPage.getNumber(),
                jobsPage.getSize(),
                jobsPage.getTotalElements(),
                jobsPage.getTotalPages(),
                jobsPage.isLast()
        );

        return new JobPagingResponse<>(
                200,
                SuccessMsg.APP_200.name(),
                "Filtered listings compiled successfully",
                path,
                pagedDataResponse
        );

    }


    private void validateCoordinates(BigDecimal lat, BigDecimal lng) {
        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 ||
                lat.compareTo(BigDecimal.valueOf(90)) > 0 ||
                lng.compareTo(BigDecimal.valueOf(-180)) < 0 ||
                lng.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new InvalidException("Latitude must be between -90 and 90 and longitude between -180 and 180");
        }
    }

    // Return the great-circle distance in kilometers; radiusInKilometers is only the search limit.
    private double calculateDistanceInKilometers(
            double userLatitude,
            double userLongitude,
            double jobLatitude,
            double jobLongitude
    ) {

        final double earthRadiusInKilometers = 6371.0088;
        double latitudeDelta = Math.toRadians(jobLatitude - userLatitude);
        double longitudeDelta = Math.toRadians(jobLongitude - userLongitude);

        double haversine = Math.pow(Math.sin(latitudeDelta / 2), 2)
                + Math.cos(Math.toRadians(userLatitude))
                * Math.cos(Math.toRadians(jobLatitude))
                * Math.pow(Math.sin(longitudeDelta / 2), 2);

        double centralAngle = 2 * Math.atan2(
                Math.sqrt(haversine),
                Math.sqrt(1 - haversine)
        );

        return earthRadiusInKilometers * centralAngle;
    }

}
