package com.localjobfinder.bakend.servicePackage;

import com.localjobfinder.bakend.apiDataPackage.SubmitReviewRequest;
import com.localjobfinder.bakend.dbEntity.ApplicationJobsEntity;
import com.localjobfinder.bakend.dbEntity.JobsEntity;
import com.localjobfinder.bakend.dbEntity.ReviewEntity;
import com.localjobfinder.bakend.dbEntity.UserEntity;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.enums.JobStatusEnum;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.notification.JobNotificationEvent;
import com.localjobfinder.bakend.notification.JobReviewNotificationDto;
import com.localjobfinder.bakend.repository.ApplicationJobsRepository;
import com.localjobfinder.bakend.repository.JobsRepository;
import com.localjobfinder.bakend.repository.ReviewRepository;
import com.localjobfinder.bakend.repository.UserRepository;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.serviceInterfacePackage.ReviewServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService implements ReviewServiceInterface  {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final JobsRepository jobsRepository;
    private final ApplicationJobsRepository applicationJobsRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReviewService(ReviewRepository reviewRepository, JobsRepository jobsRepository,
                         ApplicationJobsRepository applicationJobsRepository, UserRepository userRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.jobsRepository = jobsRepository;
        this.applicationJobsRepository = applicationJobsRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public JobResponse submitReview(Long reviewerId, SubmitReviewRequest request, String path) throws CustomException {

        // 1. Fetch job structure and check status criteria
        JobsEntity job = jobsRepository.findById(request.getJobId())
                .orElseThrow(() -> new CustomException(ErrorMsg.JOB_001, 404));

        if (!JobStatusEnum.COMPLETED.equals(job.getStatus())) {
            throw new CustomException(ErrorMsg.JOB_002, 400);
        }

        // 2. Prevent duplicate submissions across the same layout constraint bounds
        if (reviewRepository.existsByJobIdAndReviewerId(job.getId(), reviewerId)) {
            throw new CustomException(ErrorMsg.JOB_003, 409);
        }

        // 3. Find the accepted worker for the job
        ApplicationJobsEntity placement = applicationJobsRepository.findAcceptedApplicant(job.getId())
                .orElseThrow(() -> new CustomException(ErrorMsg.JOB_004, 404));

        Long workerId = placement.getUser().getId();
        Long ownerId = job.getUser().getId();

        UserEntity reviewerEntity = userRepository.getReferenceById(reviewerId);
        UserEntity receiverEntity;

        // 4. Determine Dynamic Direction Routing Strategy
        if (reviewerId.equals(ownerId)) {
            // Case A: Owner is reviewing the Worker
            receiverEntity = userRepository.getReferenceById(workerId);
        } else if (reviewerId.equals(workerId)) {
            // Case B: Worker is reviewing the Owner
            receiverEntity = userRepository.getReferenceById(ownerId);
        } else {
            // Strangers attempting access bypass
            throw new CustomException(ErrorMsg.RREVIW_001, 403);
        }

        // 5. Build and save the review structure
        ReviewEntity review = new ReviewEntity();
        review.setJob(job);
        review.setReviewer(reviewerEntity);
        review.setReceiver(receiverEntity);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        // 6. PRODUCTION CORRECTED: Asynchronously notify the receiver via unified multi-parameter payload format
        try {
            // Package structured details so the mobile app can parse them directly from the FCM data bundle
            JobReviewNotificationDto reviewPayload = new JobReviewNotificationDto(
                    job.getId(),
                    job.getTitle(),
                    reviewerEntity.getId(),
                    reviewerEntity.getUsername(),
                    request.getRating(),
                    request.getComment()
            );

            String title = "New Review Received! ⭐️";
            String msg = reviewerEntity.getUsername() + " left you a " + request.getRating() + "-star rating.";

            // Dispatches matching all 5 constructor parameters perfectly
            eventPublisher.publishEvent(new JobNotificationEvent(
                    receiverEntity.getId(), // 1. targetUserId
                    title,                  // 2. title
                    msg,                    // 3. message
                    "JOB_REVIEWED",         // 4. notificationType
                    reviewPayload           // 5. detailsPayload
            ));

            log.info("Successfully pushed paged async review event notice for receiverId={}", receiverEntity.getId());
        } catch (Exception e) {
            // Production rule: Log payload pipeline faults safely, never roll back database states for side-effect metrics
            log.error("Failed to compile asynchronous review notification event dispatch parameters: ", e);
        }

        return new JobResponse(201, "REVIEW_SUBMITTED", "Review saved successfully.", path);
    }

}
