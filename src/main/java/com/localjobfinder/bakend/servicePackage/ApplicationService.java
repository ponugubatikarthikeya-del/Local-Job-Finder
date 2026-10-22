package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.dbEntity.ApplicationJobsEntity;
import com.localjobfinder.bakend.dbEntity.JobsEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.dtoPackage.ApplicantDto;
import com.localjobfinder.bakend.enums.*;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.*;
import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;
import com.localjobfinder.bakend.repository.*;
import com.localjobfinder.bakend.redis.JobCacheNames;
import com.localjobfinder.bakend.redis.RedisCacheInvalidator;
import com.localjobfinder.bakend.responsePackage.JobResponse;

import com.localjobfinder.bakend.serviceInterfacePackage.ApplicationServiceInterface;
import com.localjobfinder.bakend.utilsPackage.MapStructMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApplicationService implements ApplicationServiceInterface {

    private final JobsRepository jobsRepository;
    private final UserRepository userRepository;
    private final ApplicationJobsRepository applicationJobsRepository;
    private final MapStructMapper mapStructMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisCacheInvalidator cacheInvalidator;

    private static final String APPLICANTS_CACHE = JobCacheNames.APPLICANTS;

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    public ApplicationService(JobsRepository jobsRepository, UserRepository userRepository,
                      ApplicationJobsRepository applicationJobsRepository,MapStructMapper mapStructMapper,
                              ApplicationEventPublisher eventPublisher,
                              RedisCacheInvalidator cacheInvalidator) {
        this.jobsRepository = jobsRepository;
        this.userRepository = userRepository;
        this.applicationJobsRepository = applicationJobsRepository;
        this.mapStructMapper = mapStructMapper;
        this.eventPublisher = eventPublisher;
        this.cacheInvalidator = cacheInvalidator;
    }

    @Override
    @Transactional
    public JobResponse applyJob(String path, Long userId, Long jobId) {

        if (userId == null || userId <= 0) {
            throw new InvalidException(ErrorMsg.AUTH_004.getMessage());
        }

        if (jobId == null || jobId <= 0) {
            throw new InvalidException(ErrorMsg.BUS_001.getMessage());
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMsg.AUTH_008.getMessage()));

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Target job entry missing"));

        if (!JobStatusEnum.OPEN.equals(job.getStatus())) {
            throw new InvalidException(ErrorMsg.BUS_003.getMessage());
        }

        if (job.getUser() != null && userId.equals(job.getUser().getId())) {
            throw new AccessDeniedException("Self-application strategy prohibited");
        }

        Optional<ApplicationJobsEntity> existingApplication = applicationJobsRepository.findByUserIdAndJobId(userId, jobId);

        if (existingApplication.isPresent()) {
            throw new DuplicateAvailableException("Duplicate entry registration attempt");
        }

        ApplicationJobsEntity application = new ApplicationJobsEntity();
        application.setUser(user);
        application.setJob(job);
        application.setStatus(ApplicationStatusEnum.APPLIED);
        applicationJobsRepository.save(application);

        cacheInvalidator.evictAfterCommit(APPLICANTS_CACHE);

        log.info("Job application created: userId={}, jobId={}", userId, jobId);

        // Notify Job Owner about the new application asynchronously

//        try {
//
//            JobApplyNotificationDto applyPayload = new JobApplyNotificationDto(
//                    job.getId(),
//                    job.getTitle(),
//                    user.getId(),
//                    user.getUsername(),
//                    user.getPhone()
//            );
//
//            String title = "New Applicant! 📩";
//
//            String message = user.getUsername() + " has applied for your job: " + job.getTitle();
//
//            eventPublisher.publishEvent(new JobNotificationEvent(
//                    job.getUser().getId(), // Target: Job Owner
//                    title,
//                    message,
//                    "JOB_APPLIED",
//                    applyPayload
//            ));
//        } catch (Exception e) {
//            log.error("Failed to queue async job application notification payload: ", e);
//        }

        return new JobResponse(200, SuccessMsg.APP_200.name(), SuccessMsg.APP_200.getMessage(), path);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = APPLICANTS_CACHE,
            key = "{#ownerId, #jobId, #page, #size, #path}",
            sync = true
    )
    public JobPagingResponse<PagedResponse<ApplicantDto>> viewApplicants(String path, Long ownerId, Long jobId,int page,int size) {

        if (ownerId == null || ownerId <= 0) {
            throw new UnauthorizedException(ErrorMsg.AUTH_004.getMessage());
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job description record absent"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new AccessDeniedException("Resource visualization limited to owner identity context");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ApplicationJobsEntity> applicationPage = applicationJobsRepository.findByJobId(jobId, pageable);

        List<ApplicantDto> applicants = applicationPage.getContent().stream().map(application -> {

            ApplicantDto applicantDto =  mapStructMapper.
                   toApplicantDtoByUserEntityAndProfileEntity(application.getUser(),application.getUser().getProfile());
            applicantDto.setApplicationId(application.getId());
            applicantDto.setAppliedAt(application.getCreatedAt());

            return applicantDto;
        }).collect(Collectors.toList());

        log.debug("Loaded applicant page from database: ownerId={}, jobId={}, page={}, size={}, count={}",
                ownerId, jobId, page, size, applicants.size());

        PagedResponse<ApplicantDto> pagedDataResponse = new PagedResponse<>(
                applicants,
                applicationPage.getNumber(),
                applicationPage.getSize(),
                applicationPage.getTotalElements(),
                applicationPage.getTotalPages(),
                applicationPage.isLast()
        );


        return new JobPagingResponse<>(
                200,
                "APPLICANTS_FETCH_SUCCESSFULLY",
                "job applicants fetched succussFully",
                path,
                pagedDataResponse
         );

    }

    @Override
    @Transactional
    public JobResponse acceptApplicant(String path, Long ownerId, Long jobId, Long applicantId) {

        if (ownerId == null || ownerId <= 0) {
            throw new UnauthorizedException(ErrorMsg.AUTH_004.getMessage());
        }

        if (jobId == null || applicantId == null) {
            throw new ResourceNotFoundException(ErrorMsg.BUS_001.getMessage());
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException( "Job structural context missing"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new AccessDeniedException(ErrorMsg.AUTHZ_001.getMessage());
        }

        if (!JobStatusEnum.OPEN.equals(job.getStatus())) {
            throw new InvalidException(ErrorMsg.BUS_001.getMessage());
        }

        ApplicationJobsEntity application = applicationJobsRepository.findByUserIdAndJobId(applicantId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("No application matching requested coordinates"));


        if(!application.getStatus().equals(ApplicationStatusEnum.APPLIED)) {
            throw new InvalidException(ErrorMsg.BUS_002.getMessage());
        }

        // 1. Accept target applicant
        application.setStatus(ApplicationStatusEnum.ACCEPTED);
        applicationJobsRepository.save(application);

        // 2. PRODUCTION STRATEGY: Do not automatically reject other applicants yet.
        // Leave them as PENDING on standby in case of flaking or cancellation.

        // 3. Shift job state tracking node
        job.setStatus(JobStatusEnum.IN_PROGRESS);
        jobsRepository.save(job);

        cacheInvalidator.evictAfterCommit(
                APPLICANTS_CACHE,
                JobCacheNames.JOB_SEARCH,
                JobCacheNames.MY_JOBS
        );

        log.info("Applicant accepted: ownerId={}, jobId={}, applicantId={}", ownerId, jobId, applicantId);

//        // 4. Fire Async push notification payload delivery via FCM integration module
//        try {
//
//            UserEntity owner = job.getUser();
//            JobAcceptNotificationDto acceptPayload = new JobAcceptNotificationDto(
//                    job.getId(),
//                    job.getTitle(),
//                    owner.getId(),
//                    owner.getUsername(),
//                    owner.getPhone()
//            );
//
//            String title = "Application Approved! 🎉";
//            String message = "Your application for '" + job.getTitle() + "' was accepted. Contact the owner now!";
//
//            eventPublisher.publishEvent(new JobNotificationEvent(
//                    applicantId, // Target: The Accepted Worker
//                    title,
//                    message,
//                    "JOB_ACCEPTED",
//                    acceptPayload
//            ));
//        } catch (Exception e) {
//            log.error("Failed to queue async job acceptance notification payload: ", e);
//        }

        return new JobResponse(200, SuccessMsg.APP_202.name(), SuccessMsg.APP_202.getMessage(), path);
    }

    @Override
    @Transactional
    public JobResponse rejectApplicant(String path, Long ownerId, Long jobId, Long applicantId) {

        if (ownerId == null || ownerId <= 0) {
            throw new UnauthorizedException(ErrorMsg.AUTH_004.getMessage());
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("job is not exist for this id"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new AccessDeniedException("Operation barred from unauthorized actor contexts");
        }

        if (!JobStatusEnum.OPEN.equals(job.getStatus())) {
            throw new InvalidException(ErrorMsg.BUS_003.getMessage());
        }

        ApplicationJobsEntity application = applicationJobsRepository.findByUserIdAndJobId(applicantId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Application mapping matching trace absent"));

        if(!application.getStatus().equals(ApplicationStatusEnum.APPLIED)) {
            throw new InvalidException(ErrorMsg.BUS_002.getMessage());
        }

        application.setStatus(ApplicationStatusEnum.REJECTED);

        applicationJobsRepository.save(application);

        cacheInvalidator.evictAfterCommit(
                APPLICANTS_CACHE,
                JobCacheNames.JOB_SEARCH,
                JobCacheNames.MY_JOBS
        );

        log.info("Applicant rejected: ownerId={}, jobId={}, applicantId={}", ownerId, jobId, applicantId);

        return new JobResponse(200, SuccessMsg.APP_202.name(), SuccessMsg.APP_202.getMessage(), path);
    }
}