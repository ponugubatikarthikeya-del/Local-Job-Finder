package com.localjobfinder.bakend.filter;

import com.localjobfinder.bakend.dtoPackage.JobDto;
import com.localjobfinder.bakend.filter.Specification.JobFiler;
import com.localjobfinder.bakend.apiDataPackage.JobFilterCriteria;
import com.localjobfinder.bakend.paging.JobPagingResponse;
import com.localjobfinder.bakend.paging.PagedResponse;
import com.localjobfinder.bakend.security.jwtTokenPackage.JwtDataProviderService;
import com.localjobfinder.bakend.utilsPackage.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/api/jobs")
public class JobFilterControllerClass {

    private final JobFiler jobFilterService;
    private final JwtDataProviderService  jwtDataProviderService;
    private String getPath(HttpServletRequest request) {
        return SecurityUtils.sanitize(request.getRequestURI());
    }

    @Autowired
    public JobFilterControllerClass(JobFiler jobFilter, JwtDataProviderService jwtDataProviderService) {
        this.jobFilterService = jobFilter;
        this.jwtDataProviderService = jwtDataProviderService;
    }

    @GetMapping("/filter")
    public ResponseEntity<JobPagingResponse<PagedResponse<JobDto>>> getFilteredJobs(
            @ModelAttribute @Valid JobFilterCriteria criteria,
//            @RequestParam BigDecimal lat,
//            @RequestParam BigDecimal lng,
//            @RequestParam(required = false, defaultValue = "10.0") Double radius,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) throws Exception {

        Long userId = jwtDataProviderService.getCurrentUserId();

        JobPagingResponse<PagedResponse<JobDto>> response = jobFilterService.getFilterJobs(
                                userId, criteria, getPath(request)
                        );

        return ResponseEntity.ok(response);
    }
}
