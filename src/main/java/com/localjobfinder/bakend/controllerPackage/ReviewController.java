package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.apiDataPackage.SubmitReviewRequest;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtSecurityService;
import com.localjobfinder.bakend.servicePackage.ReviewService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user/job/reviews/")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);


    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }


    private final ReviewService reviewService;
    private final JwtSecurityService jwtSecurityService;

    public ReviewController(ReviewService reviewService, JwtSecurityService jwtSecurityService) {
        this.reviewService = reviewService;
        this.jwtSecurityService = jwtSecurityService;
    }

    @PostMapping("submit")
    public ResponseEntity<JobResponse> submitReview(
            @Valid @RequestBody SubmitReviewRequest submitReviewRequest,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

        Long currentUserId = jwtSecurityService.getCurrentUserId();
        log.info("Current user id is {}", currentUserId);

        JobResponse response = reviewService.submitReview(currentUserId, submitReviewRequest, getPath(httpServletRequest));
        return ResponseEntity.ok(response);
    }
}
