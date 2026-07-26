package com.localjobfinder.bakend.controllerPackage;

import com.localjobfinder.bakend.enums.ErrorMsg;
import com.localjobfinder.bakend.exceptionPackage.CustomException;
import com.localjobfinder.bakend.responsePackage.AllJobsResponse;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtSecurityService;
import com.localjobfinder.bakend.servicePackage.JobFilerService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("user/job/filter/")
public class JobFilterController {

    private static final Logger log = LoggerFactory.getLogger(JobFilterController.class);

    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    private final JobFilerService jobFilterService;
    private final JwtSecurityService jwtSecurityService;

    @Autowired
    public JobFilterController(JwtSecurityService jwtSecurityService, JobFilerService jobFilterService) {
        this.jwtSecurityService = jwtSecurityService;
        this.jobFilterService = jobFilterService;
    }


    @GetMapping("getActiveJobs/nearby")
    public ResponseEntity<AllJobsResponse> getActiveJobs(
            @RequestParam("lat") BigDecimal lat,
            @RequestParam("lng") BigDecimal lng,
            @RequestParam(value = "radius", defaultValue = "20.0") Double radius,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

        log.info("getAllJobs api called with lat={}, lng={}, radius={}", lat, lng, radius);

        if (lat == null || lng == null) {
            throw new CustomException(ErrorMsg.BUS_001, 400, "latitude and longitude are required");
        }

        String path = getPath(httpServletRequest);

        Long  userId = jwtSecurityService.getCurrentUserId();

        // Instantiate page parameters safely
        Pageable pageable = PageRequest.of(page, size);

        AllJobsResponse response = jobFilterService.getActiveJobs(lat, lng, radius, path,userId,pageable);

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);

    }

    @GetMapping("getMyJobs")
    public ResponseEntity<AllJobsResponse> getMyJobs(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) throws CustomException {

        log.info("getJobs api called");

        Long userId = jwtSecurityService.getCurrentUserId();

        log.debug("authenticated userId={}", userId);

        Pageable pageable = PageRequest.of(page, size);

        AllJobsResponse userResponse = jobFilterService.getMyJobs(getPath(httpServletRequest), userId,pageable);
        return ResponseEntity.status(HttpStatusCode.valueOf(userResponse.getStatus())).body(userResponse);
    }


    @GetMapping("jobs/search")
    public ResponseEntity<AllJobsResponse> searchJobs(
            @RequestParam("lat") BigDecimal lat,
            @RequestParam("lng") BigDecimal lng,
            @RequestParam(value = "radius", defaultValue = "20.0") Double radius,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) String category,
            @org.springframework.data.web.PageableDefault(size = 20, sort = "distance") Pageable pageable,
            HttpServletRequest httpServletRequest
    ) throws CustomException {

        log.info("Job Search initiated: query='{}', category='{}', radius={}km, sort={}",
                search, category, radius, pageable.getSort());

        Long userId = jwtSecurityService.getCurrentUserId();

        AllJobsResponse response = jobFilterService.searchJobs(
                lat, lng, radius, search, category, getPath(httpServletRequest), userId, pageable
        );

        return ResponseEntity.status(HttpStatusCode.valueOf(response.getStatus())).body(response);
    }


}
