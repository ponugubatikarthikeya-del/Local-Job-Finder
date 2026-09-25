package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.responsePackage.JobResponse;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtDataProviderService;
import com.localjobfinder.bakend.serviceInterfacePackage.JobServiceInterface;
import com.localjobfinder.bakend.servicePackage.JobService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/job/")
public class JobsController {

    private static final Logger log = LoggerFactory.getLogger(JobsController.class);

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    private  final JobServiceInterface jobService;
    private final JwtDataProviderService jwtDataProviderService;

    @Autowired
    public JobsController(JobServiceInterface jobService, JwtDataProviderService jwtDataProviderService) {
        this.jobService = jobService;
        this.jwtDataProviderService = jwtDataProviderService;
    }

    @PostMapping("createJob")
    public ResponseEntity<UserResponse> postJob(
           @Valid @RequestBody PostJobRequest postJobRequest,
           HttpServletRequest httpServletRequest) {

       log.info("create job api called");

       Long userId = jwtDataProviderService.getCurrentUserId();

       log.debug("authenticated userId={}", userId);

       UserResponse response = jobService.postJob(postJobRequest, getPath(httpServletRequest), userId);

       return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

    @DeleteMapping("deleteJob/{jobId}")
    public ResponseEntity<JobResponse> deleteJob(
            @PathVariable Long jobId,
            HttpServletRequest httpServletRequest ) {
        log.info("deleteJob api called for jobId={}", jobId);

        Long userId = jwtDataProviderService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        JobResponse response = jobService.deleteJob(userId,jobId,getPath(httpServletRequest));

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


    @PatchMapping("job-completed/{jobId}")
    public ResponseEntity<JobResponse> jobComplete(
            @PathVariable Long jobId,
            HttpServletRequest httpServletRequest ) {

       log.info("jobComplete api called for jobId={}", jobId);

        Long userId = jwtDataProviderService.getCurrentUserId();

        JobResponse response = jobService.jobComplete(userId,jobId,getPath(httpServletRequest));
        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }

}
