package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.components.GeometryUtils;
import com.localjobfinder.bakend.dbEntity.ApplicationJobsEntity;
import com.localjobfinder.bakend.dbEntity.JobsEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.dtoPackage.UserDto;
import com.localjobfinder.bakend.enums.ApplicationStatusEnum;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.JobStatusEnum;
import com.localjobfinder.bakend.enums.SuccessMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.notification.JobAcceptNotificationDto;
import com.localjobfinder.bakend.notification.JobApplyNotificationDto;
import com.localjobfinder.bakend.notification.JobNotificationEvent;
import com.localjobfinder.bakend.repository.*;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.responsePackage.ViewApplicantsResponse;
import com.localjobfinder.bakend.serviceInterfacePackage.ApplicationServiceInterface;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    public ApplicationService(JobsRepository jobsRepository, UserRepository userRepository,
                      ApplicationJobsRepository applicationJobsRepository, ModelMapper modelMapper,
                              ApplicationEventPublisher eventPublisher) {
        this.jobsRepository = jobsRepository;
        this.userRepository = userRepository;
        this.applicationJobsRepository = applicationJobsRepository;
        this.modelMapper = modelMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public JobResponse applyJob(String path, Long userId, Long jobId) throws CustomException {
        if (userId == null || userId <= 0) {
            throw new CustomException(ErrorMsg.AUTH_004, 400, "User parameter missing");
        }
        if (jobId == null || jobId <= 0) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "Invalid target job configuration");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorMsg.AUTH_008, 404, "User context missing"));

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorMsg.BUS_001, 404, "Target job entry missing"));

        if (!JobStatusEnum.OPEN.equals(job.getStatus())) {
            throw new CustomException(ErrorMsg.BUS_003, 400, "Job listing is no longer open for registration");
        }

        if (job.getUser() != null && userId.equals(job.getUser().getId())) {
            throw new CustomException(ErrorMsg.AUTHZ_001, 403, "Self-application strategy prohibited");
        }

        Optional<ApplicationJobsEntity> existingApplication = applicationJobsRepository.findByUserIdAndJobId(userId, jobId);

        if (existingApplication.isPresent()) {
            throw new CustomException(ErrorMsg.BUS_004, 409, "Duplicate entry registration attempt");
        }

        ApplicationJobsEntity application = new ApplicationJobsEntity();
        application.setUser(user);
        application.setJob(job);
        application.setStatus(ApplicationStatusEnum.APPLIED);
        applicationJobsRepository.save(application);

        // Notify Job Owner about the new application asynchronously
        try {

            JobApplyNotificationDto applyPayload = new JobApplyNotificationDto(
                    job.getId(),
                    job.getTitle(),
                    user.getId(),
                    user.getUsername(),
                    user.getPhone()
            );

            String title = "New Applicant! 📩";
            String message = user.getUsername() + " has applied for your job: " + job.getTitle();

            eventPublisher.publishEvent(new JobNotificationEvent(
                    job.getUser().getId(), // Target: Job Owner
                    title,
                    message,
                    "JOB_APPLIED",
                    applyPayload
            ));
        } catch (Exception e) {
            log.error("Failed to queue async job application notification payload: ", e);
        }

        return new JobResponse(200, SuccessMsg.APP_200.name(), SuccessMsg.APP_200.getMessage(), path);
    }

    @Override
    @Transactional(readOnly = true)
    public ViewApplicantsResponse viewApplicants(String path, Long ownerId, Long jobId, Pageable pageable) throws CustomException {
        if (ownerId == null || ownerId <= 0) {
            throw new CustomException(ErrorMsg.AUTH_004, 401, "Authentication required");
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorMsg.BUS_001, 404, "Job description record absent"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new CustomException(ErrorMsg.AUTHZ_001, 403, "Resource visualization limited to owner identity context");
        }

        Page<ApplicationJobsEntity> applicationPage = applicationJobsRepository.findByJobId(jobId, pageable);

        List<UserDto> applicants = applicationPage.getContent().stream().map(application -> {
            return modelMapper.map(application.getUser(), UserDto.class);
        }).collect(Collectors.toList());

        ViewApplicantsResponse response = new ViewApplicantsResponse(200, SuccessMsg.APP_201.name(), SuccessMsg.APP_201.getMessage(), path, applicants);
        response.setTotalElements(applicationPage.getTotalElements());
        response.setTotalPages(applicationPage.getTotalPages());
        response.setPageNumber(applicationPage.getNumber());
        response.setLast(applicationPage.isLast());

        return response;
    }

    @Override
    @Transactional
    public JobResponse acceptApplicant(String path, Long ownerId, Long jobId, Long applicantId) throws CustomException {
        if (ownerId == null || ownerId <= 0) {
            throw new CustomException(ErrorMsg.AUTH_004, 401, "Authentication mapping missing");
        }
        if (jobId == null || applicantId == null) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "Target execution boundaries must be populated");
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorMsg.BUS_001, 404, "Job structural context missing"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new CustomException(ErrorMsg.AUTHZ_001, 403, "Action barred from unauthorized actor contexts");
        }

        if (!JobStatusEnum.OPEN.equals(job.getStatus())) {
            throw new CustomException(ErrorMsg.BUS_003, 400, "Job listing structural lock reached. No longer open.");
        }

        ApplicationJobsEntity application = applicationJobsRepository.findByUserIdAndJobId(applicantId, jobId)
                .orElseThrow(() -> new CustomException(ErrorMsg.BUS_001, 404, "No application matching requested coordinates"));

        // 1. Accept target applicant
        application.setStatus(ApplicationStatusEnum.ACCEPTED);
        applicationJobsRepository.save(application);

        // 2. PRODUCTION STRATEGY: Do not automatically reject other applicants yet.
        // Leave them as PENDING on standby in case of flaking or cancellation.

        // 3. Shift job state tracking node
        job.setStatus(JobStatusEnum.IN_PROGRESS);
        jobsRepository.save(job);

        // 4. Fire Async push notification payload delivery via FCM integration module
        try {

            UserEntity owner = job.getUser();
            JobAcceptNotificationDto acceptPayload = new JobAcceptNotificationDto(
                    job.getId(),
                    job.getTitle(),
                    owner.getId(),
                    owner.getUsername(),
                    owner.getPhone()
            );

            String title = "Application Approved! 🎉";
            String message = "Your application for '" + job.getTitle() + "' was accepted. Contact the owner now!";

            eventPublisher.publishEvent(new JobNotificationEvent(
                    applicantId, // Target: The Accepted Worker
                    title,
                    message,
                    "JOB_ACCEPTED",
                    acceptPayload
            ));
        } catch (Exception e) {
            log.error("Failed to queue async job acceptance notification payload: ", e);
        }

        return new JobResponse(200, SuccessMsg.APP_202.name(), SuccessMsg.APP_202.getMessage(), path);
    }

    @Override
    @Transactional
    public JobResponse rejectApplicant(String path, Long ownerId, Long jobId, Long applicantId) throws CustomException {
        if (ownerId == null || ownerId <= 0) {
            throw new CustomException(ErrorMsg.AUTH_004, 401, "Authentication mapping missing");
        }

        JobsEntity job = jobsRepository.findById(jobId)
                .orElseThrow(() -> new CustomException(ErrorMsg.BUS_001, 404, "Target context trace missing"));

        if (job.getUser() == null || !ownerId.equals(job.getUser().getId())) {
            throw new CustomException(ErrorMsg.AUTHZ_001, 403, "Operation barred from unauthorized actor contexts");
        }

        ApplicationJobsEntity application = applicationJobsRepository.findByUserIdAndJobId(applicantId, jobId)
                .orElseThrow(() -> new CustomException(ErrorMsg.BUS_001, 404, "Application mapping matching trace absent"));

        application.setStatus(ApplicationStatusEnum.REJECTED);
        applicationJobsRepository.save(application);

        return new JobResponse(200, SuccessMsg.APP_202.name(), SuccessMsg.APP_202.getMessage(), path);
    }
}