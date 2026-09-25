package com.localjobfinder.bakend.filter.Specification;

import com.localjobfinder.bakend.dbEntity.JobsEntity;
import com.localjobfinder.bakend.dbEntity.ProfilesEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.InvalidException;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.ResourceNotFoundException;
import com.localjobfinder.bakend.paging.JobFilterCriteria;
import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;
import com.localjobfinder.bakend.paging.SpecificationBuilder;
import com.localjobfinder.bakend.repository.JobsRepository;
import com.localjobfinder.bakend.repository.ProfileRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.utilsPackage.MapStructMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobFiler {

    private final JobsRepository jobsRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Autowired
    private MapStructMapper mapStructMapper;

    @Autowired
    private GeometryFactory geometryFactory;

    public JobFiler(JobsRepository jobsRepository, UserRepository userRepository,
                    ProfileRepository profileRepository) {
        this.jobsRepository = jobsRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public JobPagingResponse<PagedResponse<JobDto>> getFilterJobs(
            Long userId,JobFilterCriteria criteria, String path) throws Exception {

        if (path == null || criteria.getLat() == null || criteria.getLng() == null) {
            throw new InvalidException("Search location vectors cannot be null");
        }

        // 1. Convert the incoming Kilometer radius value to Meters for MySQL
        double radiusInMeters = (criteria.getRadius() != null ? criteria.getRadius() : 10.0) * 1000.0;

        if (userId == null || userId <= 0) {
            throw new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage());
        }

        try {

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage()));

            // Update user tracking metrics
            ProfilesEntity profile = user.getProfile();

            if (profile != null) {
                profile.setLatitude(criteria.getLat());
                profile.setLongitude(criteria.getLng());
                profileRepository.save(profile);
            }

            // 3. Formulate Spring Data Pageable structure parameters
            Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), Sort.by("createdAt").descending());

            // 4. Instantiate user point structure context utilizing your geometryFactory bean
            // Note: (longitude, latitude) coordinate order sequence is required for SRID 4326
            Point userLocation = geometryFactory.createPoint(new Coordinate(criteria.getLng().doubleValue(), criteria.getLat().doubleValue()));

            // 5. Build dynamic criteria specifications
            Specification<JobsEntity> spec = SpecificationBuilder.filterBy(
                    criteria, radiusInMeters, userLocation
            );

            // 6. Execute pagination block query targeting spatial indices
            Page<JobsEntity> jobsPage = jobsRepository.findAll(spec, pageable);

            // 7. Transpile JPA structural entities into explicit client data transfer objects
            List<JobDto> dtoList = jobsPage.getContent().stream()
                    .map(mapStructMapper::toJobDtoByJobEntity)
                    .collect(Collectors.toList());

            // 8. Bundle results inside your custom pagination response objects
            PagedResponse<JobDto> pagedDataResponse = new PagedResponse<>(
                    dtoList,
                    jobsPage.getNumber(),
                    jobsPage.getSize(),
                    jobsPage.getTotalElements(),
                    jobsPage.getTotalPages(),
                    jobsPage.isLast()
            );

            return new JobPagingResponse<>(200,
                    SuccessMsg.APP_200.name(),
                    "Filtered listings compiled successfully",
                    path,
                    pagedDataResponse);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
