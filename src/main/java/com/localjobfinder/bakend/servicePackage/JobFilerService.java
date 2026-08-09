package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.components.GeometryUtils;
import com.localjobfinder.bakend.dbEntity.JobsEntity;
import com.localjobfinder.bakend.dbEntity.ProfilesEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.*;
import com.localjobfinder.bakend.responsePackage.AllJobsResponse;
import com.localjobfinder.bakend.serviceInterfacePackage.JobFilterServiceInterface;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobFilerService implements JobFilterServiceInterface {

    private final JobsRepository jobsRepository;
    private final UserRepository userRepository;
    private final ActiveJobsListRepository activeJobsListRepository;
    private final ProfileRepository profileRepository;
    private final GeometryUtils geometryUtils;
    private final ModelMapper modelMapper;

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    @Autowired
    public JobFilerService(JobsRepository jobsRepository, UserRepository userRepository,
                      GeometryUtils geometryUtils, ActiveJobsListRepository activeJobsListRepository,
                      ProfileRepository profileRepository, ModelMapper modelMapper) {
        this.jobsRepository = jobsRepository;
        this.userRepository = userRepository;
        this.activeJobsListRepository = activeJobsListRepository;
        this.geometryUtils = geometryUtils;
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public AllJobsResponse getActiveJobs(BigDecimal lat, BigDecimal lng, Double radius, String path, Long userId, Pageable pageable) throws CustomException {
        if (path == null || lat == null || lng == null) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "Search location vectors cannot be null");
        }
        if (radius == null || radius <= 0) {
            radius = 10.0;
        }
        if (userId == null || userId <= 0) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "User context identity missing");
        }

        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008, 404, "User not found"));

            ProfilesEntity profilesEntity = user.getProfile();

            if (profilesEntity != null) {
                profilesEntity.setLatitude(lat);
                profilesEntity.setLongitude(lng);
                profileRepository.save(profilesEntity);
            }

            Page<NearbyJobProjection> dynamicPageResult = activeJobsListRepository.findNearbyActiveJobs(lat, lng, radius, pageable);

            List<JobDto> jobsList = dynamicPageResult.getContent().stream().map(jobProjection -> {
                JobDto cardDto = modelMapper.map(jobProjection, JobDto.class);
                cardDto.setUserId(jobProjection.getUserId());
                cardDto.setUsername(jobProjection.getUsername());

                Double rawDistance = jobProjection.getDistance();
                cardDto.setDistance(rawDistance != null ? Math.round(rawDistance * 100.0) / 100.0 : 0.0);
                return cardDto;
            }).collect(Collectors.toList());

            AllJobsResponse response = new AllJobsResponse(200, SuccessMsg.JOB_200.name(), SuccessMsg.JOB_200.getMessage(), path, jobsList);
            response.setTotalElements(dynamicPageResult.getTotalElements());
            response.setTotalPages(dynamicPageResult.getTotalPages());
            response.setLast(dynamicPageResult.isLast());
            response.setPageNumber(dynamicPageResult.getNumber());

            return response;
        } catch (CustomException ex) {
            throw new CustomException(ErrorMsg.BUS_001, 500, "Error fetching nearby jobs: ");
        } catch (Exception e) {
            log.error("Failed executing paged geo-spatial query: ", e);
            throw new CustomException(ErrorMsg.BUS_001, 500, "Error fetching nearby jobs: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AllJobsResponse getMyJobs(String path, Long userId, Pageable pageable) throws CustomException {
        if (userId == null || userId <= 0) {
            throw new CustomException(ErrorMsg.AUTH_004, 400, "Invalid user token pointer");
        }
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorMsg.AUTH_008, 404, "User context not found");
        }

        try {
            Page<JobsEntity> jobsPage = jobsRepository.findByUserId(userId, pageable);

            List<JobDto> dtoList = jobsPage.getContent().stream().map(job -> {
                JobDto dto = modelMapper.map(job, JobDto.class);
                dto.setUserId(userId);
                dto.setUsername(job.getUser().getUsername());
                dto.setDistance(0.0);
                return dto;
            }).collect(Collectors.toList());

            AllJobsResponse response = new AllJobsResponse(200, SuccessMsg.JOB_200.name(), SuccessMsg.JOB_200.getMessage(), path, dtoList);
            response.setTotalElements(jobsPage.getTotalElements());
            response.setTotalPages(jobsPage.getTotalPages());
            response.setPageNumber(jobsPage.getNumber());
            response.setLast(jobsPage.isLast());

            return response;
        } catch (Exception e) {
            log.error("Failed to fetch user listings: ", e);
            throw new CustomException(ErrorMsg.BUS_001, 500, "Error retrieving job listings: " + e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public AllJobsResponse searchJobs(BigDecimal lat, BigDecimal lng, Double radius, String search,
                                      String category, String path, Long userId, Pageable pageable) throws CustomException {

        if (lat == null || lng == null) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "Device coordinates are required to calculate job proximities.");
        }

        String searchParam = (search == null || search.trim().isBlank()) ? null : search.trim();
        String categoryParam = (category == null || category.trim().isBlank()) ? null : category.trim();
        double radiusParam = (radius == null || radius <= 0) ? 10.0 : radius;

        try {
            // 1. Calculate explicit limit and offset values to bypass automated sorting appends
            int limit = pageable.getPageSize();
            long offset = pageable.getOffset();

            // 2. Fetch the data content list using explicit pagination
            List<NearbyJobProjection> searchResults = activeJobsListRepository.searchNearbyJobs(
                    lat, lng, radiusParam, searchParam, categoryParam, limit, offset
            );

            // 3. Fetch the accurate count using the companion count query setup
            long totalElements = activeJobsListRepository.countNearbyJobs(
                    lat, lng, radiusParam, searchParam, categoryParam
            );

            // 4. Map projections cleanly to your card payload formats
            List<JobDto> jobsList = searchResults.stream().map(jobProjection -> {
                JobDto cardDto = modelMapper.map(jobProjection, JobDto.class);
                Double rawDistance = jobProjection.getDistance();
                cardDto.setDistance(rawDistance != null ? Math.round(rawDistance * 100.0) / 100.0 : 0.0);
                return cardDto;
            }).collect(Collectors.toList());

            // 5. Assemble response wrapper with pagination metrics calculated from totalElements
            AllJobsResponse response = new AllJobsResponse(
                    200,
                    SuccessMsg.JOB_200.name(),
                    SuccessMsg.JOB_200.getMessage(),
                    path,
                    jobsList
            );

            int totalPages = (int) Math.ceil((double) totalElements / limit);
            int pageNumber = pageable.getPageNumber();

            response.setTotalElements(totalElements);
            response.setTotalPages(totalPages == 0 ? 1 : totalPages);
            response.setPageNumber(pageNumber);
            response.setLast(pageNumber >= (totalPages - 1));

            return response;

        } catch (Exception e) {
            log.error("Search execution failure across filters: ", e);
            throw new CustomException(ErrorMsg.BUS_001, 500, ErrorMsg.BUS_001.getMessage());
        }
    }


}
