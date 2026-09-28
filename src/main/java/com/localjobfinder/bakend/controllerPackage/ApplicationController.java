package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.dtoPackage.ApplicantDto;

import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;
import com.localjobfinder.bakend.responsePackage.JobResponse;

import com.localjobfinder.bakend.security.jwtTokenPackage.JwtDataProviderService;
import com.localjobfinder.bakend.serviceInterfacePackage.ApplicationServiceInterface;

import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final ApplicationServiceInterface applicationService;
    private final JwtDataProviderService jwtDataProviderService;

    @Autowired
    public ApplicationController(ApplicationServiceInterface applicationService, JwtDataProviderService jwtDataProviderService) {
        this.applicationService = applicationService;
        this.jwtDataProviderService = jwtDataProviderService;
    }

    @PostMapping("applyJob/{jobId}")
    public ResponseEntity<JobResponse> applyJob(
            @PathVariable Long jobId,
            HttpServletRequest httpServletRequest) {

        log.info("applyJob api called for jobId={}", jobId);

        Long userId = jwtDataProviderService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        JobResponse userResponse = applicationService.applyJob(getPath(httpServletRequest), userId, jobId);
        return ResponseEntity.status(HttpStatusCode.valueOf(userResponse.getStatus())).body(userResponse);
    }


    @GetMapping("viewApplicants/{jobId}")
    public ResponseEntity<JobPagingResponse<PagedResponse<ApplicantDto>>> viewApplicants(
            @PathVariable Long jobId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            HttpServletRequest httpServletRequest) {

        log.info("viewApplicants api called for jobId={}", jobId);

        Long userId = jwtDataProviderService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        JobPagingResponse<PagedResponse<ApplicantDto>> response = applicationService
                .viewApplicants(getPath(httpServletRequest), userId, jobId,page,size);

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    @PostMapping("acceptApplicant/{jobId}/{applicantId}")
    public ResponseEntity<JobResponse> acceptApplicant(
            @PathVariable Long jobId,
            @PathVariable Long applicantId,
            HttpServletRequest httpServletRequest ) {

        log.info("acceptApplicant api called for jobId={}, applicantId={}", jobId, applicantId);

        Long userId = jwtDataProviderService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        JobResponse response = applicationService.acceptApplicant(getPath(httpServletRequest), userId, jobId, applicantId);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    @PostMapping("rejectApplicant/{jobId}/{applicantId}")
    public ResponseEntity<JobResponse> rejectApplicant(
            @PathVariable Long jobId,
            @PathVariable Long applicantId,
            HttpServletRequest httpServletRequest ) {

        log.info("rejectApplicant api called for jobId={}, applicantId={}", jobId, applicantId);

        Long userId = jwtDataProviderService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        JobResponse response = applicationService.rejectApplicant(getPath(httpServletRequest), userId, jobId, applicantId);
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

}
