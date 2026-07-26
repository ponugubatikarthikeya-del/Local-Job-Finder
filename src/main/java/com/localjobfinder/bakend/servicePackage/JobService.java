package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.components.GeometryUtils;
import com.localjobfinder.bakend.dbEntity.*;
import com.localjobfinder.bakend.dtoPackage.JobCardDto;
import com.localjobfinder.bakend.dtoPackage.CompleteJobDto;
import com.localjobfinder.bakend.enums.*;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.repository.*;
import com.localjobfinder.bakend.responsePackage.AllJobsResponse;
import com.localjobfinder.bakend.responsePackage.JobDetailsResponse;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.serviceInterfacePackage.JobServiceInterface;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;


@Service
public class JobService implements JobServiceInterface {

    private final JobsRepository jobsRepository;
    private final UserRepository userRepository;
    private final ActiveJobsListRepository activeJobsListRepository;
    private final ProfileRepository profileRepository;
    private final GeometryUtils geometryUtils;
    private final ModelMapper modelMapper;

    private final ApplicationJobsRepository applicationJobsRepository;

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    @Autowired
    public JobService(JobsRepository jobsRepository, UserRepository userRepository,
                      GeometryUtils geometryUtils, ActiveJobsListRepository activeJobsListRepository,
                      ProfileRepository profileRepository,
                      ApplicationJobsRepository applicationJobsRepository, ModelMapper modelMapper) {
        this.jobsRepository = jobsRepository;
        this.userRepository = userRepository;
        this.activeJobsListRepository = activeJobsListRepository;
        this.geometryUtils = geometryUtils;
        this.modelMapper = modelMapper;
        this.profileRepository = profileRepository;
        this.applicationJobsRepository = applicationJobsRepository;
    }

    @Override
    @Transactional
    public UserResponse postJob(PostJobRequest postJobRequest, String path, Long userId) throws CustomException {
        if (userId == null || userId <= 0) {
            throw new CustomException(ErrorMsg.AUTH_004, 400, "User ID is null or empty");
        }

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008, 404, "User context not found"));

        // Validation Constraints
        if (postJobRequest.getTitle() == null || postJobRequest.getTitle().isBlank()) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "Job title cannot be empty");
        }
        if (postJobRequest.getPayAmount() == null || postJobRequest.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "Pay amount must be greater than 0");
        }
        if (postJobRequest.getLatitude() == null || postJobRequest.getLongitude() == null) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "Location coordinates are required");
        }

        JobsEntity postJob = new JobsEntity();
        postJob.setTitle(postJobRequest.getTitle());
        postJob.setDescription(postJobRequest.getDescription());
        postJob.setCategory(postJobRequest.getCategory());
        postJob.setPayAmount(postJobRequest.getPayAmount());
        postJob.setJobType(postJobRequest.getJobType());
        postJob.setLatitude(postJobRequest.getLatitude());
        postJob.setLongitude(postJobRequest.getLongitude());
        postJob.setAddress(postJobRequest.getAddress());
        postJob.setJobTime(postJobRequest.getJobTime());
        postJob.setStatus(JobStatusEnum.OPEN);
        postJob.setUser(userEntity);

        JobsEntity savedJob = jobsRepository.save(postJob);

        ActiveJobsListEntity listing = new ActiveJobsListEntity();
        listing.setJob(savedJob);
        listing.setOwner(userEntity);
        activeJobsListRepository.save(listing);

        CompleteJobDto dto = modelMapper.map(savedJob, CompleteJobDto.class);
        dto.setUserId(userEntity.getId());
        dto.setUsername(userEntity.getUsername());

        return new UserResponse(201, SuccessMsg.JOB_201.name(), SuccessMsg.JOB_201.getMessage(), path, dto);
    }

    @Override
    @Transactional
    public JobResponse deleteJob(Long ownerId, Long jobId, String path) throws CustomException {
        if (ownerId == null || ownerId <= 0) {
            throw new CustomException(ErrorMsg.AUTH_004, 401, "Authentication required");
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorMsg.BUS_001, 404, "Job listing not found"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new CustomException(ErrorMsg.AUTHZ_001, 403, "Unauthorized layout alter request");
        }

        job.setStatus(JobStatusEnum.DELETED);
        jobsRepository.save(job);

        activeJobsListRepository.deleteByJobId(jobId);

        return new JobResponse(200, SuccessMsg.JOB_203.name(), SuccessMsg.JOB_203.getMessage(), path);
    }

    @Override
    @Transactional
    public JobResponse jobComplete(Long ownerId, Long jobId, String path) throws CustomException {
        if (ownerId == null || ownerId <= 0) {
            throw new CustomException(ErrorMsg.AUTH_004, 401, "Authentication credentials required");
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorMsg.BUS_001, 404, "Job record not found"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new CustomException(ErrorMsg.AUTHZ_001, 403, "Access Denied: Only job poster can execute completion");
        }

        if (job.getStatus() == JobStatusEnum.DELETED || job.getStatus() == JobStatusEnum.COMPLETED) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "Job terminal condition reached already");
        }

        job.setStatus(JobStatusEnum.COMPLETED);
        jobsRepository.save(job);

        // Feed Cleanup
        activeJobsListRepository.deleteByJobId(jobId);

        // PRODUCTION CLEAN SWEEP: Bulk update standby/pending candidates to REJECTED now that job finalized
        applicationJobsRepository.updateStatusForOthers(jobId, null, ApplicationStatusEnum.REJECTED);

        return new JobResponse(200, SuccessMsg.JOB_204.name(), "Job marked as completed successfully", path);
    }

    @Override
    @Transactional(readOnly = true)
    public JobDetailsResponse getJobDetails(String path, Long userId, Long jobId) throws CustomException {

        if (userId == null || userId <= 0) {
            throw new CustomException(ErrorMsg.AUTH_004, 401, "Authentication credentials required.");
        }

        if (jobId == null || jobId <= 0) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "Target Job ID is missing or invalid.");
        }

        // 1. Fetch the complete job entity context
        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorMsg.BUS_001, 404, "Target job listing not found."));

        try {
            // 2. Map core parameters directly using ModelMapper
            CompleteJobDto detailsDto = modelMapper.map(job, CompleteJobDto.class);

            // Map user properties explicitly to avoid lazy-loading initialization issues
            if (job.getUser() != null) {
                detailsDto.setUserId(job.getUser().getId());
                detailsDto.setUsername(job.getUser().getUsername());
            }

//            // 3. Evaluate context boundaries (Is the requester the creator?)
//            boolean isOwner = job.getUser() != null && userId.equals(job.getUser().getId());
////            detailsDto.setOwner(isOwner);

//            // 4. Determine application tracking state for the requesting user
//            if (isOwner) {
//                detailsDto.setMyApplicationStatus("OWNER");
//            } else {
//                Optional<ApplicationJobsEntity> application =
//                        applicationJobsRepository.findByUserIdAndJobId(userId, jobId);
//
//                if (application.isPresent()) {
//                    detailsDto.setMyApplicationStatus(application.get().getStatus().name());
//                } else {
//                    detailsDto.setMyApplicationStatus("NOT_APPLIED");
//                }
//            }

            // 5. Package output response payload layout
            return new JobDetailsResponse(
                    200,
                    SuccessMsg.JOB_200.name(),
                    "Job configuration details parsed successfully.",
                    path,
                    detailsDto
            );

        } catch (Exception e) {
            log.error("Failed executing job description profile parsing for jobId={}", jobId, e);
            throw new CustomException(ErrorMsg.BUS_001, 500, "Error resolving job profile details: " + e.getMessage());
        }
    }

}


