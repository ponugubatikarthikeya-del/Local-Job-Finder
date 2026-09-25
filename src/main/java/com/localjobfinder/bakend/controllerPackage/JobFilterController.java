package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.dbEntity.JobsEntity;
import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.exceptionPackage.custom_exceptions.InvalidException;
import com.localjobfinder.bakend.paging.JobFilterCriteria;
import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;
import com.localjobfinder.bakend.responsePackage.AllJobsResponse;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtDataProviderService;
import com.localjobfinder.bakend.serviceInterfacePackage.JobFilterServiceInterface;
import com.localjobfinder.bakend.servicePackage.JobFilerService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("user/job/filter/")
public class JobFilterController {

    private static final Logger log = LoggerFactory.getLogger(JobFilterController.class);

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    private final JobFilterServiceInterface jobFilterService;
    private final JwtDataProviderService jwtDataProviderService;

    @Autowired
    public JobFilterController(JwtDataProviderService jwtDataProviderService, JobFilterServiceInterface jobFilterService) {
        this.jwtDataProviderService = jwtDataProviderService;
        this.jobFilterService = jobFilterService;
    }

    @GetMapping("getActiveJobs/nearby")
    public ResponseEntity<JobPagingResponse<PagedResponse<JobDto>>> getFilteredJobs(
            @ModelAttribute @Valid JobFilterCriteria criteria,
            HttpServletRequest httpServletRequest) {

        log.info("Getting filter jobs API called");

        Long userId = jwtDataProviderService.getCurrentUserId();

        JobPagingResponse<PagedResponse<JobDto>> response = jobFilterService.filterJobs(
                userId, criteria, getPath(httpServletRequest)
        );

        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }


    @GetMapping("getMyJobs")
    public ResponseEntity<JobPagingResponse<PagedResponse<JobDto>>> getMyJobs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            HttpServletRequest httpServletRequest
    ) {

        log.info("getMyJobs api called");

        Long userId = jwtDataProviderService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        JobPagingResponse<PagedResponse<JobDto>> response =
                jobFilterService.getMyJobs(userId,getPath(httpServletRequest),page,size);

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


}
