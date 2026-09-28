package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.dbEntity.*;

import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.enums.*;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.AccessDeniedException;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.UnauthorizedException;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.InvalidException;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.ResourceNotFoundException;
import com.localjobfinder.bakend.repository.*;
import com.localjobfinder.bakend.redis.RedisCacheInvalidator;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.responsePackage.JobDataResponse;
import com.localjobfinder.bakend.serviceInterfacePackage.JobServiceInterface;
import com.localjobfinder.bakend.utilsPackage.MapStructMapper;
import com.localjobfinder.bakend.components.GeometryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class JobService implements JobServiceInterface {

    private final JobsRepository jobsRepository;
    private final UserRepository userRepository;
    private final ActiveJobsListRepository activeJobsListRepository;
    private final MapStructMapper mapStructMapper;
    private final GeometryUtils geometryUtils;

    private final ApplicationJobsRepository applicationJobsRepository;
    private final RedisCacheInvalidator cacheInvalidator;

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    @Autowired
    public JobService(JobsRepository jobsRepository, UserRepository userRepository,ActiveJobsListRepository activeJobsListRepository,
                      ApplicationJobsRepository applicationJobsRepository,MapStructMapper mapStructMapper,
                      GeometryUtils geometryUtils, RedisCacheInvalidator cacheInvalidator ){
        this.jobsRepository = jobsRepository;
        this.userRepository = userRepository;
        this.activeJobsListRepository = activeJobsListRepository;
        this.mapStructMapper = mapStructMapper;
        this.geometryUtils = geometryUtils;
        this.applicationJobsRepository = applicationJobsRepository;
        this.cacheInvalidator = cacheInvalidator;
    }

    @Override
    @Transactional
    public JobResponse postJob(PostJobRequest postJobRequest, String path, Long userId)  {

        if (userId == null || userId <= 0) {
            throw new InvalidException("User ID is null or empty");
        }

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage()));

        // Validation Constraints
        if (postJobRequest.getTitle() == null || postJobRequest.getTitle().isBlank()) {
            throw new InvalidException("Job title cannot be empty");
        }

        if (postJobRequest.getPayAmount() == null || postJobRequest.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidException("Pay amount must be greater than 0");
        }

        if (postJobRequest.getLatitude() == null || postJobRequest.getLongitude() == null) {
            throw new InvalidException("Location coordinates are required");
        }

        if (postJobRequest.getAddress() == null || postJobRequest.getAddress().isBlank()) {
            throw new InvalidException("Job address is required");
        }

        JobsEntity postJob = mapStructMapper.toJobsEntityByPostJobRequest(postJobRequest);

        postJob.setStatus(JobStatusEnum.OPEN);
        postJob.setUser(userEntity);
        postJob.setAddress(postJobRequest.getAddress().trim());
        postJob.setLocation(geometryUtils.createPoint(
                postJobRequest.getLatitude(), postJobRequest.getLongitude()));

        JobsEntity savedJob = jobsRepository.save(postJob);

        ActiveJobsListEntity listing = new ActiveJobsListEntity();

        listing.setJob(savedJob);
        listing.setOwner(userEntity);
        activeJobsListRepository.save(listing);
        cacheInvalidator.evictAfterCommit("jobs", "myJob");
        log.info("Job posted: ownerId={}, jobId={}", userId, savedJob.getId());

//        JobDto dto = mapStructMapper.toJobDtoByJobEntity(savedJob);
//
//        dto.setUserId(userEntity.getId());
//        dto.setUsername(userEntity.getUsername());

        return new JobResponse(201,
                SuccessMsg.JOB_201.name(),
                SuccessMsg.JOB_201.getMessage(),
                path
        );
    }

    @Override
    @Transactional
    public JobResponse deleteJob(Long ownerId, Long jobId, String path) {

        if (ownerId == null || ownerId <= 0) {
            throw new UnauthorizedException(ErrorMsg.AUTH_004.getMessage());
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException( "Job listing not found"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new AccessDeniedException(ErrorMsg.AUTHZ_001.getMessage());
        }

        job.setStatus(JobStatusEnum.DELETED);

        activeJobsListRepository.deleteByJobId(jobId);

//        Flushing Strategy: Calling .flush() on your active listings table ensures the deletion
//        query fires instantly, preventing race conditions if the UI immediately requests a refreshed job feed.

        activeJobsListRepository.flush();
        cacheInvalidator.evictAfterCommit("jobs", "myJob");
        log.info("Job deleted: ownerId={}, jobId={}", ownerId, jobId);

//        Saves IO: Removing .save() prevents Hibernate
//        from running redundant select checks or double update operations on the jobs table context.

//        jobsRepository.save(job);

        return new JobResponse(200, SuccessMsg.JOB_203.name(), SuccessMsg.JOB_203.getMessage(), path);
    }

    @Override
    @Transactional
    public JobResponse jobComplete(Long ownerId, Long jobId, String path){

        if (ownerId == null ||  ownerId <= 0) {
            throw new UnauthorizedException(ErrorMsg.AUTH_004.getMessage());
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException( "Job record not found"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new AccessDeniedException( "Access Denied: Only job poster can execute completion");
        }

        if (job.getStatus() == JobStatusEnum.DELETED || job.getStatus() == JobStatusEnum.COMPLETED) {
            throw new InvalidException( "Job terminal condition reached already");
        }

        job.setStatus(JobStatusEnum.COMPLETED);

        jobsRepository.save(job);

        // Feed Cleanup
        activeJobsListRepository.deleteByJobId(jobId);

        // PRODUCTION CLEAN SWEEP: Bulk update standby/pending candidates to REJECTED now that job finalized
        applicationJobsRepository.updateStatusForOthers(jobId, null, ApplicationStatusEnum.REJECTED);
        cacheInvalidator.evictAfterCommit("jobs", "myJob", "applicants:v2");
        log.info("Job completed: ownerId={}, jobId={}", ownerId, jobId);

        return new JobResponse(200, SuccessMsg.JOB_204.name(), "Job marked as completed successfully", path);
    }

}
