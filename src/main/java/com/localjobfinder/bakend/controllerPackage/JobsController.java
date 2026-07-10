package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.AllJobsResponse;
import com.localjobfinder.bakend.responsePackage.JobDetailsResponse;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.security.SecurityService;
import com.localjobfinder.bakend.servicePackage.JobService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("user/job/")
public class JobsController {

    private static final Logger log = LoggerFactory.getLogger(JobsController.class);

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    private  final JobService jobService;
    private final SecurityService securityService;

    @Autowired
    public JobsController(JobService jobService, SecurityService securityService) {
        this.jobService = jobService;
        this.securityService = securityService;
    }

    @PostMapping("createJob")
    public ResponseEntity<UserResponse> postJob(
           @Valid @RequestBody PostJobRequest postJobRequest,
           HttpServletRequest httpServletRequest) throws CustomException {

       log.info("create job api called");

       Long userId = securityService.getCurrentUserId();

       log.debug("authenticated userId={}", userId);

       UserResponse response = jobService.postJob(postJobRequest, getPath(httpServletRequest), userId);

       return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    @DeleteMapping("deleteJob/{jobId}")
    public ResponseEntity<JobResponse> deleteJob(
            @PathVariable Long jobId,
            HttpServletRequest httpServletRequest
    ) throws CustomException {
        log.info("deleteJob api called for jobId={}", jobId);

        Long userId = securityService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);
        JobResponse response = jobService.deleteJob(userId,jobId,getPath(httpServletRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    @PatchMapping("job-completed/{JobId}")
    public ResponseEntity<JobResponse> jobComplete(
            @PathVariable Long jobId,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

       log.info("jobComplete api called for jobId={}", jobId);

        Long userId = securityService.getCurrentUserId();

        JobResponse response = jobService.jobComplete(userId,jobId,getPath(httpServletRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    @GetMapping("job-details/{jobId}")
    public ResponseEntity<JobDetailsResponse> getJobDetails(
            @PathVariable("jobId") Long jobId,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

        log.info("getJobDetails api called for jobId={}", jobId);

        String path = getPath(httpServletRequest);
        Long userId = securityService.getCurrentUserId();

        JobDetailsResponse response = jobService.getJobDetails(path, userId, jobId);

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


}
