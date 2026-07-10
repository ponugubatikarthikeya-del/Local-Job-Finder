package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.responsePackage.ViewApplicantsResponse;
import com.localjobfinder.bakend.security.SecurityService;
import com.localjobfinder.bakend.servicePackage.ApplicationService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/job/applications/")
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    private final ApplicationService applicationService;
    private final SecurityService securityService;

    @Autowired
    public ApplicationController(ApplicationService applicationService, SecurityService securityService) {
        this.applicationService = applicationService;
        this.securityService = securityService;
    }


    @PostMapping("applyJob/{jobId}")
    public ResponseEntity<JobResponse> applyJob(
            @PathVariable Long jobId,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

        log.info("applyJob api called for jobId={}", jobId);

        Long userId = securityService.getCurrentUserId();
        log.debug("authenticated userId={}", userId);

        JobResponse userResponse = applicationService.applyJob(getPath(httpServletRequest), userId, jobId);
        return ResponseEntity.status(HttpStatusCode.valueOf(userResponse.getStatus())).body(userResponse);
    }


    @GetMapping("viewApplicants/{jobId}")
    public ResponseEntity<ViewApplicantsResponse> viewApplicants(
            @PathVariable Long jobId,
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) throws CustomException {

        log.info("viewApplicants api called for jobId={}", jobId);

        Long userId = securityService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        Pageable pageable = PageRequest.of(page, size);

        ViewApplicantsResponse response = applicationService.viewApplicants(getPath(httpServletRequest), userId, jobId,pageable);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    @PostMapping("acceptApplicant/{jobId}/{applicantId}")
    public ResponseEntity<JobResponse> acceptApplicant(
            @PathVariable Long jobId,
            @PathVariable Long applicantId,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

        log.info("acceptApplicant api called for jobId={}, applicantId={}", jobId, applicantId);

        Long userId = securityService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        JobResponse response = applicationService.acceptApplicant(getPath(httpServletRequest), userId, jobId, applicantId);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    @PostMapping("rejectApplicant/{jobId}/{applicantId}")
    public ResponseEntity<JobResponse> rejectApplicant(
            @PathVariable Long jobId,
            @PathVariable Long applicantId,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

        log.info("rejectApplicant api called for jobId={}, applicantId={}", jobId, applicantId);

        Long userId = securityService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        JobResponse response = applicationService.rejectApplicant(getPath(httpServletRequest), userId, jobId, applicantId);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

}
