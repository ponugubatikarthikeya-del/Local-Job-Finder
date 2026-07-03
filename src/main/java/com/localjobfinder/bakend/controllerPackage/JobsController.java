package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.apiDataPackage.PostJobRequest;
import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.UserResponse;
import com.localjobfinder.bakend.servicePackage.JobService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/job")
public class JobsController {

    private static final Logger log = LoggerFactory.getLogger(JobsController.class);

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    private  final JobService jobService;

    @Autowired
    public JobsController(JobService jobService) {
        this.jobService = jobService;
    }

    // post a job api
    @PostMapping("createJob")
    public ResponseEntity<UserResponse> postJob(
            @Valid @RequestBody PostJobRequest postJobRequest,
            Authentication authentication,HttpServletRequest httpServletRequest) throws CustomException {

        log.info("create job api called");

        // get userId for jwt token
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new CustomException(ErrorMsg.AUTH_004, 401);
        }

       String userId = (String) authentication.getPrincipal();

        log.debug("authenticated userId={}", userId);

        UserResponse response = jobService.postJob(postJobRequest,getPath(httpServletRequest),userId);

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

//    @GetMapping("getJobs")
//    public ResponseEntity<UserResponse> getJobs(@Valid
//            Authentication authentication,HttpServletRequest httpServletRequest
//    ) throws CustomException {
//
//        // get userId for jwt token
//        if (authentication == null || authentication.getPrincipal() == null) {
//            throw new CustomException(ErrorMsg.AUTH_004, 401);
//        }
//
//        String userId = (String) authentication.getPrincipal();
//
//        log.debug("authenticated userId={}", userId);
//        UserResponse userResponse = jobService.getJobs(getPath(httpServletRequest),userId);
//        return ResponseEntity.status(HttpStatusCode.valueOf(userResponse.getStatus())).body(userResponse);
//    }
//
//    // for users apply job list
//    @PostMapping("applyJob/{jobId}")
//    public ResponseEntity<UserResponse> applyJob(
//            @PathVariable Long jobId,Authentication authentication,HttpServletRequest httpServletRequest
//    ) throws CustomException {
//        if (authentication == null || authentication.getPrincipal() == null) {
//            throw new CustomException(ErrorMsg.AUTH_004, 401);
//        }
//        String userId = (String) authentication.getPrincipal();
//        log.debug("authenticated userId={}", userId);
//        UserResponse userResponse = jobService.applyJob(getPath(httpServletRequest),userId,jobId);
//        return ResponseEntity.status(HttpStatusCode.valueOf(userResponse.getStatus())).body(userResponse);
//
//    }
}
